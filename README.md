# EBook Reader

一个功能完善的电子书阅读器应用，支持多种格式的电子书阅读。

## 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── ebookreader/
│   │   │               ├── data/
│   │   │               │   ├── model/          // 数据模型
│   │   │               │   ├── local/          // 本地数据源
│   │   │               │   └── repository/     // 数据仓库
│   │   │               ├── domain/             // 业务逻辑
│   │   │               │   ├── parser/         // 文件解析
│   │   │               │   └── pagination/     // 分页算法
│   │   │               └── ui/
│   │   │                   ├── reader/         // 阅读界面
│   │   │                   ├── bookshelf/      // 书架界面
│   │   │                   └── settings/       // 设置界面
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_main.xml
│   │       │   ├── reader_activity.xml
│   │       │   └── item_book.xml
│   │       └── menu/
│   │           └── reader_toolbar.xml
```

## 功能特性

- 支持多种电子书格式（EPUB、TXT等）
- 智能分页算法
- 书架管理
- 阅读设置（字体大小、背景颜色等）
- 阅读进度保存

## 技术栈

- Android
- Clean Architecture
- MVVM 架构模式
- Room 数据库
- Jsoup（HTML解析）

## 开发环境要求

- Android Studio
- JDK 11+
- Android SDK 30+ 