# AwesomeTool
Android线程切换工具

---
## 介绍说明

曾经使用过AndroidAnnotations,对里面的线程切换方式很是喜欢，使用非常方便，可是由于各种原因，已经不再想使用AndroidAnnotations了，
但又舍不得这种便捷的方式，于是就实现了自己的线程切换。
* 原理很简单，就是使用HandlerThread以及Handler来做线程的切换
* 加入对注解的支持
* 使用AnnotationProcessor插件在编译期生成代码，无性能影响

---
## 使用说明

* 使用@InjectObject声明注入对象,命名则是根据注解所在类的类名加上后缀“Helper”
```
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @InjectObject
    MainActivityHelper helper;
    
     ...
}

```

* 使用@WorkInBackground和@WorkInMainThread声明方法，其中方法必须是public void xxx(){}格式

```
@WorkInBackground
public void needWorkInThread() {
//耗时操作
}

@WorkInMainThread
public void needWorkInMainThread() {
//更新UI操作
}

```

* 调用inject方法传入目标类

```
 AwesomeTool.inject(this);
 
```

* 在编译期会自动注入MainActivityHelper实例，调用其中的方法即可

```
helper.needWorkInThread();
helper.needWorkInMainThread();

```

* 不用时请记得调用quit

```
helper.quit();
```
---
## 后期目标

* 考虑对方法参数作支持
* 考虑对返回参数作支持

---
## JavaDoc文档

* [在线JavaDoc](https://jitpack.io/com/github/huweijian5/AwesomeTool/1.0.0/javadoc/index.html)
* 网址：`https://jitpack.io/com/github/huweijian5/AwesomeTool/[VersionCode]/javadoc/index.html`
* 其中[VersionCode](https://github.com/huweijian5/AwesomeTool/releases)请替换为最新版本号
* 注意文档使用UTF-8编码，如遇乱码，请在浏览器选择UTF-8编码即可

---
## 引用

* 如果需要引用此库,做法如下：
* Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```	
* and then,add the dependecy:
```
dependencies {
	        compile 'com.github.huweijian5:AwesomeTool:latest_version'
		annotationProcessor 'com.github.huweijian5:AwesomeTool-compiler:latest_version'
}
```
* 其中latest_version请到[releases](https://github.com/huweijian5/AwesomeTool/releases)中查看

## 注意
* 为了避免引入第三方库导致工程依赖多个版本的问题，如android support库
* 故建议在个人的工程目录下的build.gradle下加入以下变量，具体请看此[build.gradle](https://github.com/huweijian5/AwesomeTool/blob/master/build.gradle)
```
ext{
    minSdkVersion = 16
    targetSdkVersion = 25
    compileSdkVersion = 25
    buildToolsVersion = '25.0.1'

    // App dependencies
    supportLibraryVersion = '25.0.1'
    junitVersion = '4.12'
    espressoVersion = '2.2.2'
}
```	
* 请注意，对于此库已有的变量，命名请保持一致


