[app]
title = Knowledge App
package.name = knowledgeapp
package.domain = com.luoxiaoliangtj
source.dir = .
source.include_exts = py,png,jpg,kv,atlas,html,css,js,json,txt,md
version = 1.0.0
requirements = python3,kivy,requests,pillow,urllib3,certifi,charset_normalizer,idna
orientation = portrait
fullscreen = 0
android.permissions = INTERNET,READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE,MANAGE_EXTERNAL_STORAGE
android.api = 33
android.minapi = 21
android.ndk = 25b
android.arch = arm64-v8a
android.allow_backup = True
p4a.local_recipes = 
ios.kivy_ios_url = https://github.com/kivy/kivy-ios
ios.kivy_ios_branch = master
ios.ios_deploy_url = https://github.com/phonegap/ios-deploy
ios.ios_deploy_branch = 1.12.2
[buildozer]
log_level = 2
warn_on_root = 1
