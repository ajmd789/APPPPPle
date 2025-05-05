# APPPPPle - Android 电子书阅读器

## 项目概述
APPPPPle 是一个简单的 Android 电子书阅读器应用，支持 EPUB 格式的电子书阅读。应用提供了基本的阅读功能，包括分页显示、翻页等。

## 主要功能
1. 书籍选择
   - 支持从设备中选择 EPUB 格式的电子书文件
   - 通过系统文件选择器进行文件选择

2. 书籍解析
   - 支持 EPUB 格式的解析
   - 使用 epublib 库进行 EPUB 文件解析
   - 提取书籍标题、章节内容等信息

3. 阅读功能
   - 分页显示书籍内容
   - 支持上一页/下一页翻页
   - 显示当前阅读进度
   - 自适应屏幕尺寸的排版

## 代码结构
```
app/src/main/
├── java/com/example/appppple/
│   ├── domain/
│   │   ├── model/
│   │   │   └── Book.java        # 书籍数据模型
│   │   ├── parser/
│   │   │   ├── BookParser.java  # 书籍解析器接口
│   │   │   ├── EpubParser.java  # EPUB 解析器实现
│   │   │   └── ParserFactory.java # 解析器工厂
│   │   └── pagination/
│   │       └── PaginationManager.java # 分页管理器
│   └── ui/
│       ├── MainActivity.java    # 主界面，用于选择书籍
│       └── reader/
│           └── ReaderActivity.java # 阅读器界面
└── res/
    ├── layout/
    │   ├── activity_main.xml    # 主界面布局
    │   └── reader_activity.xml  # 阅读器界面布局
    └── values/
        └── strings.xml          # 字符串资源
```

## 主要类说明

### 1. 数据模型
- `Book`: 表示书籍的基本信息，包含标题和章节列表
- `Chapter`: 表示书籍的章节，包含标题和内容

### 2. 解析器
- `BookParser`: 定义了解析器的接口
- `EpubParser`: 实现了 EPUB 格式的解析
- `ParserFactory`: 根据文件类型创建对应的解析器

### 3. 分页管理
- `PaginationManager`: 负责将书籍内容按照屏幕尺寸进行分页

### 4. 界面
- `MainActivity`: 应用入口，提供书籍选择功能
- `ReaderActivity`: 阅读器界面，显示书籍内容并处理翻页操作

## 技术特点
1. 使用工厂模式实现解析器的扩展
2. 采用 MVP 架构设计
3. 支持自适应屏幕尺寸的分页
4. 使用 Android 原生组件实现界面

## 依赖库
- epublib: 用于解析 EPUB 文件
- Jsoup: 用于处理 HTML 内容

## 使用说明
1. 启动应用
2. 点击"选择书籍"按钮
3. 从设备中选择 EPUB 格式的电子书
4. 开始阅读，使用按钮进行翻页

## 注意事项
- 目前仅支持 EPUB 格式
- 建议使用标准格式的 EPUB 文件
- 大文件可能需要较长的加载时间 