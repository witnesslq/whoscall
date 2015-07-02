#来电助手 Who's call#


### 软件功能 ###

* 类似于国外whoscall功能,通过搜索引擎识别陌生来电,目前主要是服务器端功能实现, 手机端仅包括 android demo
* 仅适用于中国大陆地区

### 编绎方式 ###
* 本人开发环境: OSX 10.9 + Intellij Idea 14.1 + JDK 1.8 + Maven 3
* 运行方式:安装maven,切换到ansj1.4/ansj_seg-master目录,运行mvn assembly:assembly,然后运行target里的jar文件
  打开浏览器 http://localhost:8080/ 即可, 如果希望变更端口,请更改com.tianlupan.whoscall.ansjServer里的设置

### 版权声明 ###

* 本程序基于 ansj_seg 分词实现, ansj_seg具有中文分词及识别中文姓名功能,网址:https://github.com/NLPchina/ansj_seg
  本程序在此基础上完善了中文公司名称识别及行业识别,地址识别的功能
* 本程序可自由使用

##

### 效果演示 ###
* [演示网址](http://www.tianlupan.com:8080/)

### Author ###
tianlupan  Email:tianlupan@126.com
