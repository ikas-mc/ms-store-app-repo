# ms-store-app-repo

本项目用于收集windows 10 商店应用并生成推荐列表页面

## 列表页面:

[https://ikas-mc.github.io/ms-store-app-repo/](https://ikas-mc.github.io/ms-store-app-repo/)

## Manifest:

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

## 页面生成:

```
//模板语法 https://pebbletemplates.io/
template/index.html
```

```shell
//java 版本 jdk14+
mvn compile && mvn exec:java  -Dlog4j.skipJansi=false -Dexec.mainClass=ikas.project.java.ms.App 
```

