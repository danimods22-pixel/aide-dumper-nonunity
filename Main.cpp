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

uintptr_t getBaseAddress;

bool(*this_ScreenResolution)(...);
bool ScreenResolution(int width, int height, bool fullscreen)
{
    return true;
}
ESP espOverlay;
std::vector<void*> players;

void clearPlayers() {
    std::vector<void*> pls;
    for (int i = 0; i < players.size(); i++) {
        if (players[i] != NULL) {
            pls.push_back(players[i]);
        }
    }
    players = pls;
}

bool playerFind(void *pl) {
    if (pl != NULL) {
        for (int i = 0; i < players.size(); i++) {
            if (pl == players[i]) return true;
        }
    }
    return false;
}




void (*SetResolution)(int width, int height, bool fullscreen);
void *(*get_transform)(void *instance);
Vector3 (*get_position)(void *instance);
void (*set_position)(void *instance, Vector3 value);
Vector3 (*WorldToScreen)(void *camera, Vector3 position);
void *(*get_main)();
int (*get_gamewidth)();
int (*get_gameheight)();

Vector3 GetPlayerLocation(void *player) {
    return get_position(get_transform(player));
}

Color espColor = Color(255,100,100,255);
Color espLineColor = Color::Red();
Color clr = Color(255,100,100,255);
float me = 25.1;
bool Esp = false;
bool EspLine = false;
bool EspBox = false;


void DrawESP(ESP esp, int screenWidth, int screenHeight) {
	esp.DrawText(espColor, "", Vector2(screenWidth / 2, screenHeight/ me), 25);
	if (!Esp) return;
	if (Esp) {
		for (int i = 0; i < players.size(); ++i) {
		void* Player = players[i];
        void* camera = get_main();
        Vector3 playerPosition = get_position(get_transform(Player));
        Vector3 playerFeetPos = WorldToScreen(camera, playerPosition);
        Vector3 playerHeadPos = WorldToScreen(camera, {playerPosition.X, playerPosition.Y + 2.5f, playerPosition.Z});
        if (playerFeetPos.Z < 0.1f || playerHeadPos.Z < 0.1f) continue;
        playerFeetPos.Y = screenHeight - playerFeetPos.Y;
        playerHeadPos.Y = screenHeight - playerHeadPos.Y;
        float boxHeight = playerFeetPos.Y - playerHeadPos.Y;
        float boxWidth = boxHeight / 2.0f;
        Rect PlayerRect(playerHeadPos.X - (boxWidth / 2), playerHeadPos.Y, boxWidth, boxHeight);
		if (EspLine) {
			esp.DrawLine(espLineColor, 2.2f, Vector2(screenWidth / 2, 0), Vector2(playerHeadPos.X, playerHeadPos.Y));
		}
	    if (EspBox) {
			esp.DrawBox(espLineColor, 2.0f, PlayerRect);
		}
		}
	}
}



void (*old_Player_Update)(...);
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
  return old_Player_Update(player);
}

void (*old_Player_Ondestroy)(...);
void Player_Ondestroy(void *player) {
    if (player != NULL) {
        old_Player_Ondestroy(player);
        players.clear();   
    }
}




void *hack_thread(void *) {
    while (!unityMap.isValid()) {
    unityMap = KittyMemory::getLibraryBaseMap("libunity.so");
    il2cppMap = KittyMemory::getLibraryBaseMap("libil2cpp.so");
    sleep(1);
  }
    sleep(5);
    Il2CppAttach();
    
    
    get_main = (void *(*)())(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Camera", "get_main", 0);
    WorldToScreen = (Vector3(*)(void *, Vector3))(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Camera", "WorldToScreenPoint", 1);
    get_transform = (void *(*)(void *))(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Component", "get_transform", 0);
    get_position = (Vector3(*)(void *))(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Transform", "get_position", 0);
    set_position = (void(*)(void *, Vector3))(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Transform", "set_position", 1);
    get_gamewidth = (int(*)())(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Screen", "get_width", 0);
    get_gameheight = (int(*)())(uintptr_t)Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Screen", "get_height", 0);
    
    
    
    DobbyHook(Il2CppGetMethodOffset("UnityEngine.CoreModule.dll", "UnityEngine", "Screen", "SetResolution", 3), (void *) ScreenResolution, (void **) &this_ScreenResolution);
    
 
    DobbyHook(Il2CppGetMethodOffset("Assembly-CSharp.dll", "", "ManAI", "Update", 0), (void *) Player_Update, (void **) &old_Player_Update);
    
    DobbyHook(Il2CppGetMethodOffset("Assembly-CSharp.dll", "", "ManAI", "OnDestroy", 0), (void *) Player_Ondestroy, (void **) &old_Player_Ondestroy);
    




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

    //Now you dont have to manually update the number everytime;
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

    //BE CAREFUL NOT TO ACCIDENTLY REMOVE break;

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
    // Create a new thread so it does not block the main thread, means the game would not freeze
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
