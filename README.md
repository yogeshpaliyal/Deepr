# Deepr 🔗

Deepr is a native Android application designed to streamline the management and testing of deeplinks. It provides a simple and efficient way to store, organize, and launch deeplinks, making it an essential tool for developers and testers.

![./assets/deepr-cover.png](./assets/deepr-cover.png)

## Download
You can download the latest version of the application from the [releases page](https://github.com/yogeshpaliyal/Deepr/releases).

## Features

- **Save and Organize Deeplinks:** Easily store and manage a list of frequently used deeplinks.
- **Launch Deeplinks:** Test and verify deeplink behavior by launching them directly from the app.
- **Search:** Quickly find specific deeplinks from your saved list.
- **Sort:** Organize your deeplinks by date or open counter in either ascending or descending order.
- **Open Counter:** Keep track of how many times each deeplink has been opened.
- **Home Screen Shortcuts:** Create shortcuts for your most-used deeplinks on your device's home screen for quick access.

## Architecture

The application is built using modern Android development practices and libraries:

- **UI:** The user interface is built entirely with **Jetpack Compose**, providing a modern and declarative approach to UI development.
- **Navigation:** **Jetpack Compose Navigation 3** is used for navigating between screens in the app.
- **ViewModel:** **Android ViewModel** is used to manage UI-related data and handle the state of the application.
- **Database:** **SQLDelight** is used for local data persistence, offering a lightweight and type-safe SQL database solution.
- **Dependency Injection:** **Koin** is used for dependency injection to promote a modular and testable architecture.
- **Asynchronous Operations:** **Kotlin Coroutines** are used for managing background threads and handling asynchronous operations smoothly.

## ✍️ Author

👤 **Yogesh Choudhary Paliyal**

* Twitter: <a href="https://twitter.com/yogeshpaliyal" target="_blank">@yogeshpaliyal</a>
* Email: yogeshpaliyal.foss@gmail.com

Feel free to ping me 😉

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

1. Open an issue first to discuss what you would like to change.
1. Fork the Project
1. Create your feature branch (`git checkout -b feature/amazing-feature`)
1. Commit your changes (`git commit -m 'Add some amazing feature'`)
1. Push to the branch (`git push origin feature/amazing-feature`)
1. Open a pull request

Please make sure to update tests as appropriate.

## 📝 License

```
MIT License

Copyright (c) 2021 Yogesh Choudhary Paliyal

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
