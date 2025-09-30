# Deepr 🔗

![./fastlane/metadata/android/en-US/images/featureGraphic.png](./fastlane/metadata/android/en-US/images/featureGraphic.png)

> Deepr is a native Android application designed to streamline the management and testing of links. It provides a simple and efficient way to store, organize, and open links.

[![Github Releases](https://img.shields.io/github/v/release/yogeshpaliyal/Deepr?style=for-the-badge)](https://github.com/yogeshpaliyal/Deepr/releases/latest)
[![Latest Master](https://img.shields.io/badge/Master-master?color=7885FF&label=Build&logo=android&style=for-the-badge)](https://github.com/yogeshpaliyal/Deepr/releases/download/latest-master/app-debug.apk)
[![Android Weekly](https://img.shields.io/badge/Android%20Weekly-%23685-2CA3E6.svg?style=for-the-badge)](http://androidweekly.net/issues/issue-685)    
[![BlueSky Follow](https://img.shields.io/badge/Bluesky-Follow-blue?style=for-the-badge&logo=bluesky&logoColor=%23fff&color=%23333&labelColor=%230285FF)](https://bsky.app/profile/yogeshpaliyal.com)
[![Twitter Follow](https://img.shields.io/twitter/follow/yogeshpaliyal?label=Follow&style=social)](https://twitter.com/intent/follow?screen_name=yogeshpaliyal)
![GitHub followers](https://img.shields.io/github/followers/yogeshpaliyal)

## 🎩 🪄 Features
- Search
- Sort
- Open Counter
- Home Screen Shortcuts
- Import/Export links
- QR Code support: Generate and scan
- Organize links by tags
- Save link by sharing from other app (eg: chrome, etc.)
- Save links to markdown file in local storage. (can be used for obsidian)

## 🏗️ Tech Stack

The application is built using modern Android development practices and libraries:

- **UI:** Jetpack Compose
- **Navigation:** Jetpack Compose Navigation 3
- **ViewModel:** Android ViewModel
- **Database:** SQLDelight
- **Dependency Injection:** Koin
- **Asynchronous Operations:** Kotlin Coroutines

## 📲 Download
You can download from any of the sources mentioned below.  
All these sources supports cross platform app updates. for eg: if you've installed app from F-Droid then you can update the app from any of the sources.
  
- F-Droid : [Download](https://f-droid.org/packages/com.yogeshpaliyal.deepr/)
- Github Release : [Download](https://github.com/yogeshpaliyal/Deepr/releases/latest)
- Play Store : [Download](https://play.google.com/store/apps/details?id=com.yogeshpaliyal.deepr.pro)

> [!IMPORTANT]
> Need more testers on play store, to make app live on play store, if you're interested to become a tester please join [Google Group for Deepr Internal Testers](https://groups.google.com/u/0/g/deepr-internal-testers)
> The process is simple Join the group, you will be add you to the internal testers list automatically, then you can download the app from this [playstore link](https://play.google.com/store/apps/details?id=com.yogeshpaliyal.deepr)

## ✍️ Author

👤 **Yogesh Choudhary Paliyal**

* Twitter: <a href="https://twitter.com/yogeshpaliyal" target="_blank">@yogeshpaliyal</a>
* Email: yogeshpaliyal.foss@gmail.com

Feel free to ping me 😉

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

1. Open an issue first to discuss what you would like to change.
2. Fork the Project
3. Create your feature branch (`git checkout -b feature/amazing-feature`)
4. Check lint issues before commiting with command `./gradlew lintKotlin`.
   - If there are using run to auto fix `./gradlew formatKotlin`. If the errors are still there resolve them manually. 
6. Commit your changes (`git commit -m 'Add some amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a pull request

Please make sure to update tests as appropriate.