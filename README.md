本项目用于收集windows 10 商店应用并生成推荐列表页面

## 列表页面:

[https://ikas-mc.github.io/ms-store-app-repo/](https://ikas-mc.github.io/ms-store-app-repo/)

## 应用描述

采用json,字段如下
```json
{
"name": "name",
"author": "",
"price": "0",
"desc": "",
"category": "",
"url": "https://www.microsoft.com/store/apps/{id}",
"productId": "{id}",
"productImage": "",
"previewImages": []
}
```
每个应用单独一个描述文件
```
//应用描述目录
repo
```

## edge插件

在edge扩展页面,打开开发者模式,然后选择载入解压的插件文件夹,

在windows 应用页面,点击插件图标,选择生成即可
```
//扩展目录
extension
```


## 页面模板:

```
//模板语法 https://pebbletemplates.io/
template/index.html
```


## 页面生成:

需要安装maven与java14+

```shell
//jdk14+
mvn compile && mvn exec:java  -Dlog4j.skipJansi=false -Dexec.mainClass=ikas.project.java.ms.App 
```
## 推荐页面:
```
//最终生成页面目录
docs
```
