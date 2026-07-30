#include <list>
#include <vector>
#include <string.h>
#include <pthread.h>
#include <thread>
#include <cstring>
#include <jni.h>
#include <unistd.h>
#include <fstream>
#include <iostream>
#include <dlfcn.h>
#include "Includes/Logger.h"
#include "Includes/obfuscate.h"
#include "KittyMemory/MemoryPatch.h"
#include "KittyMemory/kittyScanner.h"
#include "KittyMemory/kittyUtils.h"

#include "Includes/Utils.h"
#include "Menu/Setup.h"
#include "Dobby/dobby.h"
#include "AutoHook/Il2Cpp.h"
#include "AutoHook/Tools.h"
#include "EasyPatch.h"
#include "DrawingESP/ESP.h"
#include "Includes/Vector3.h"


#include "Includes/monoString.h" 
#include "Includes/Strings.h" 
//Target lib here
#define targetLibName OBFUSCATE("libil2cpp.so")

struct MemPatches {
    MemoryPatch UnlockAllSkin,UnlockAllLevel,MaxUpgrade,Upgrade;
    
} daniMods;

uintptr_t getBaseAddress = 0;

// ORIGINAL: bool(*this_ScreenResolution)(...);
// Replaced with proper signature pointer (store pointer to original SetResolution)
void (*this_ScreenResolution)(int, int, bool) = nullptr;
void (*SetResolution)(int width, int height, bool fullscreen) = nullptr;

ESP espOverlay;
std::vector<void*> players;

void clearPlayers() {
    std::vector<void*> pls;
    for (size_t i = 0; i < players.size(); i++) {
        if (players[i] != NULL) {
            pls.push_back(players[i]);
        }
    }
    players = pls;
}

bool playerFind(void *pl) {
    if (pl != NULL) {
        for (size_t i = 0; i < players.size(); i++) {
            if (pl == players[i]) return true;
        }
    }
    return false;
}

void *(*get_transform)(void *instance) = nullptr;
Vector3 (*get_position)(void *instance) = nullptr;
void (*set_position)(void *instance, Vector3 value) = nullptr;
Vector3 (*WorldToScreen)(void *camera, Vector3 position) = nullptr;
void *(*get_main)() = nullptr;
int (*get_gamewidth)() = nullptr;
int (*get_gameheight)() = nullptr;

// Optional health getter resolved via Il2Cpp (best-effort)
float (*GetPlayerHealthFunc)(void *player) = nullptr;

Vector3 GetPlayerLocation(void *player) {
    return get_position(get_transform(player));
}

float GetPlayerHealth(void *player) {
    if (!player) return -1.0f;
    if (GetPlayerHealthFunc) {
        return GetPlayerHealthFunc(player);
    }
    return -1.0f;
}

Color espColor = Color(255,100,100,255);
Color espLineColor = Color::Red();
Color clr = Color(255,100,100,255);
float me = 25.1f;
bool Esp = false;
bool EspLine = false;
bool EspBox = false;

// Forward declare old hooks
void (*old_Player_Update)(...)=nullptr;
void (*old_Player_Ondestroy)(...)=nullptr;

// ScreenResolution hook: forward to original if available (do not block game resolution changes)
void ScreenResolution(int width, int height, bool fullscreen)
{
    if (this_ScreenResolution) {
        // call original SetResolution so the game handles resolution normally
        this_ScreenResolution(width, height, fullscreen);
        return;
    }
    // fallback: do nothing
}

// Improved DrawESP: scales game coordinates to overlay, flips Y, shows health and ESPCount circle
void DrawESP(ESP esp, int overlayWidth, int overlayHeight) {
    // Debug / header text
    esp.DrawText(espColor, "", Vector2(overlayWidth / 2, overlayHeight / me), 25);

    // Compute visible players count (use WorldToScreen Z check)
    int visibleCount = 0;
    for (size_t idx = 0; idx < players.size(); ++idx) {
        void* P = players[idx];
        if (!P) continue;
        void* camera = get_main ? get_main() : nullptr;
        if (!camera) continue;
        Vector3 pPos = get_position(get_transform(P));
        Vector3 s = WorldToScreen(camera, pPos);
        if (s.Z > 0.1f) ++visibleCount;
    }

    // Draw ESPCount bubble (top-left)
    Vector2 countCenter(50.0f, 50.0f);
    float radius = 32.0f;
    // Try to draw circle; if implementation does not support DrawCircle this will be a no-op or compile error:
    // If compile error occurs, replace with DrawBox for a quick fallback.
    esp.DrawCircle(Color::Blue(), 3.0f, countCenter, radius);
    // Draw count text centered
    {
        char buf[16];
        snprintf(buf, sizeof(buf), "%d", visibleCount);
        esp.DrawText(Color::White(), buf, countCenter, 22);
    }

    if (!Esp) return;

    // get game resolution (use overlay as fallback)
    int gameW = (get_gamewidth) ? get_gamewidth() : overlayWidth;
    int gameH = (get_gameheight) ? get_gameheight() : overlayHeight;
    if (gameW == 0 || gameH == 0) { gameW = overlayWidth; gameH = overlayHeight; }

    float scaleX = (float)overlayWidth / (float)gameW;
    float scaleY = (float)overlayHeight / (float)gameH;

    // iterate players and draw boxes/lines/health
    for (size_t i = 0; i < players.size(); ++i) {
        void* Player = players[i];
        void* camera = get_main ? get_main() : nullptr;
        if (!Player || !camera) continue;

        Vector3 playerPosition = get_position(get_transform(Player));
        Vector3 playerFeetPos = WorldToScreen(camera, playerPosition);
        Vector3 playerHeadPos = WorldToScreen(camera, {playerPosition.X, playerPosition.Y + 2.5f, playerPosition.Z});

        if (playerFeetPos.Z < 0.1f || playerHeadPos.Z < 0.1f) continue;

        // scale and flip Y (Unity bottom-left -> overlay top-left)
        playerFeetPos.X *= scaleX; playerFeetPos.Y *= scaleY;
        playerHeadPos.X *= scaleX; playerHeadPos.Y *= scaleY;
        playerFeetPos.Y = overlayHeight - playerFeetPos.Y;
        playerHeadPos.Y = overlayHeight - playerHeadPos.Y;

        float boxHeight = playerFeetPos.Y - playerHeadPos.Y;
        if (boxHeight <= 0.0f) continue;
        float boxWidth = boxHeight / 2.0f;
        Rect PlayerRect(playerHeadPos.X - (boxWidth / 2.0f), playerHeadPos.Y, boxWidth, boxHeight);

        if (EspLine) {
            esp.DrawLine(espLineColor, 2.2f, Vector2(overlayWidth / 2.0f, 0.0f), Vector2(playerHeadPos.X, playerHeadPos.Y));
        }
        if (EspBox) {
            esp.DrawBox(espLineColor, 2.0f, PlayerRect);
        }

        // Draw health to the right of the box if we have a getter
        float health = GetPlayerHealth(Player);
        if (health >= 0.0f) {
            char hbuf[32];
            snprintf(hbuf, sizeof(hbuf), "HP: %d", (int)health);
            float midY = playerHeadPos.Y + (boxHeight * 0.5f);
            Vector2 healthPos(playerHeadPos.X + boxWidth/2.0f + 8.0f, midY);
            esp.DrawText(Color::Red(), hbuf, healthPos, 16);
        }
    }
}


// Player hooks (unchanged logic, only pointers initialized above)
void Player_Update(void *player) {
  if (player != NULL) {
    if (Esp) {
        if (!playerFind(player)) players.push_back(player);
        if (players.size() > 999) {
            players.clear();
        }
    } else {
        players.clear();
    }
  } else {
    clearPlayers();
  }
  if (old_Player_Update) old_Player_Update(player);
}

void Player_Ondestroy(void *player) {
    if (player != NULL) {
        if (old_Player_Ondestroy) old_Player_Ondestroy(player);
        players.clear();
    }
}


// Background thread to attach & hook
void *hack_thread(void *) {
    while (!unityMap.isValid()) {
        unityMap = KittyMemory::getLibraryBaseMap("libunity.so");
        il2cppMap = KittyMemory::getLibraryBaseMap("libil2cpp.so");
        sleep(1);
    }
    sleep(5);
    Il2CppAttach();

    // Resolve common methods
    get_main = (void *(*)())(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Camera", "get_main", 0);
    WorldToScreen = (Vector3(*)(void *, Vector3))(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Camera", "WorldToScreenPoint", 1);
    get_transform = (void *(*)(void *))(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Component", "get_transform", 0);
    get_position = (Vector3(*)(void *))(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Transform", "get_position", 0);
    set_position = (void(*)(void *, Vector3))(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Transform", "set_position", 1);
    get_gamewidth = (int(*)())(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Screen", "get_width", 0);
    get_gameheight = (int(*)())(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Screen", "get_height", 0);

    // Resolve player health getter (try common names)
    GetPlayerHealthFunc = (float(*)(void*))(uintptr_t)Il2CppGetMethodOffset("Assembly-CSharp.dll", "", "ManAI", "get_Health", 0);
    if (!GetPlayerHealthFunc) {
        GetPlayerHealthFunc = (float(*)(void*))(uintptr_t)Il2CppGetMethodOffset("Assembly-CSharp.dll", "", "ManAI", "get_health", 0);
    }

    // Hook SetResolution and keep original pointer in this_ScreenResolution
    uintptr_t setResAddr = Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Screen", "SetResolution", 3);
    if (setResAddr) {
        DobbyHook((void*)setResAddr, (void *) ScreenResolution, (void **) &this_ScreenResolution);
        // store a typed alias for convenience
        SetResolution = this_ScreenResolution;
    }

    // Hook player update/onDestroy
    uintptr_t updAddr = Il2CppGetMethodOffset("Assembly-CSharp.dll", "", "ManAI", "Update", 0);
    if (updAddr) DobbyHook((void*)updAddr, (void *) Player_Update, (void **) &old_Player_Update);

    uintptr_t ondAddr = Il2CppGetMethodOffset("Assembly-CSharp.dll", "", "ManAI", "OnDestroy", 0);
    if (ondAddr) DobbyHook((void*)ondAddr, (void *) Player_Ondestroy, (void **) &old_Player_Ondestroy);

    return nullptr;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_android_support_Menu_DrawOn(JNIEnv *env, jclass type, jobject espView, jobject canvas) {
    espOverlay = ESP(env, espView, canvas);
    if (espOverlay.isValid()){
        DrawESP(espOverlay, espOverlay.getWidth(), espOverlay.getHeight());
    }
}


jobjectArray GetFeatureList(JNIEnv *env, jobject context) {
    jobjectArray ret;

    const char *features[] = {
            OBFUSCATE("Collapse_ESP"),
            OBFUSCATE("0_CollapseAdd_ButtonOnOff_ Enable Esp"),
            OBFUSCATE("1_CollapseAdd_ButtonOnOff_ Esp Line"),
            OBFUSCATE("2_CollapseAdd_ButtonOnOff_ Esp Box"),
    };

    int Total_Feature = (sizeof features / sizeof features[0]);
    ret = (jobjectArray)
            env->NewObjectArray(Total_Feature, env->FindClass(OBFUSCATE("java/lang/String")),
                                env->NewStringUTF(""));

    for (int i = 0; i < Total_Feature; i++)
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(features[i]));

    return (ret);
}

void Changes(JNIEnv *env, jclass clazz, jobject obj,
                                        jint featNum, jstring featName, jint value,
                                        jboolean boolean, jstring str) {

    LOGD(OBFUSCATE("Feature name: %d - %s | Value: = %d | Bool: = %d | Text: = %s"), featNum,
         env->GetStringUTFChars(featName, 0), value,
         boolean, str != NULL ? env->GetStringUTFChars(str, 0) : "");

    switch (featNum) {
            case 0:
            Esp = boolean;
            break;
            case 1:
            EspLine = boolean;
            break;
            case 2:
            EspBox = boolean;
            break;
    }
}

__attribute__((constructor))
void lib_main() {
    pthread_t ptid;
    pthread_create(&ptid, NULL, hack_thread, NULL);
}

int RegisterMenu(JNIEnv *env) {
    JNINativeMethod methods[] = {
            {OBFUSCATE("Icon"), OBFUSCATE("()Ljava/lang/String;"), reinterpret_cast<void *>(Icon)},
            {OBFUSCATE("IconWebViewData"),  OBFUSCATE("()Ljava/lang/String;"), reinterpret_cast<void *>(IconWebViewData)},
            {OBFUSCATE("IsGameLibLoaded"),  OBFUSCATE("()Z"), reinterpret_cast<void *>(isGameLibLoaded)},
            {OBFUSCATE("Init"),  OBFUSCATE("(Landroid/content/Context;Landroid/widget/TextView;Landroid/widget/TextView;)V"), reinterpret_cast<void *>(Init)},
            {OBFUSCATE("SettingsList"),  OBFUSCATE("()[Ljava/lang/String;"), reinterpret_cast<void *>(SettingsList)},
            {OBFUSCATE("GetFeatureList"),  OBFUSCATE("()[Ljava/lang/String;"), reinterpret_cast<void *>(GetFeatureList)},
    };

    jclass clazz = env->FindClass(OBFUSCATE("com/android/support/Menu"));
    if (!clazz)
        return JNI_ERR;
    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0)
        return JNI_ERR;
    return JNI_OK;
}

int RegisterPreferences(JNIEnv *env) {
    JNINativeMethod methods[] = {
            {OBFUSCATE("Changes"), OBFUSCATE("(Landroid/content/Context;ILjava/lang/String;IZLjava/lang/String;)V"), reinterpret_cast<void *>(Changes)},
    };
    jclass clazz = env->FindClass(OBFUSCATE("com/android/support/Preferences"));
    if (!clazz)
        return JNI_ERR;
    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0)
        return JNI_ERR;
    return JNI_OK;
}

int RegisterMain(JNIEnv *env) {
    JNINativeMethod methods[] = {
            {OBFUSCATE("CheckOverlayPermission"), OBFUSCATE("(Landroid/content/Context;)V"), reinterpret_cast<void *>(CheckOverlayPermission)},
    };
    jclass clazz = env->FindClass(OBFUSCATE("com/android/support/Main"));
    if (!clazz)
        return JNI_ERR;
    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0)
        return JNI_ERR;

    return JNI_OK;
}

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (RegisterMenu(env) != 0)
        return JNI_ERR;
    if (RegisterPreferences(env) != 0)
        return JNI_ERR;
    if (RegisterMain(env) != 0)
        return JNI_ERR;
    return JNI_VERSION_1_6;
}
