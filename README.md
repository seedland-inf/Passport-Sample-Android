
## 目录

* [使用](#USE)
    * [添加代码仓库](#REPO)
    * [添加依赖](#DEPENENCY)
    * [添加开发者支持](#SUPPORT)
    * [混淆规则](#PROGUARD)
* [文档](#DOC)
    * [1.初始化](#INITIAL)
    * [2.开启自动刷新token](#UPDATE-TOKEN)
    * [3.调用](#CALL) 
        * [检查登录状态](#CHECK-LOGIN)
        * [注册](#REGISTER)
        * [密码登录](#LOGIN-PASSWORD)
        * [验证码登录](#LOGIN-CAPTCHA)
        * [重置（忘记）密码](#PASSWORD-RESET)
        * [修改密码](#PASSWORD-MODIFY)
        * [获取Token](#TOKEN)
        * [登出](#LOGOUT)

## <a name="USE"></a>使用
[![](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16) [![](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html) ![](https://img.shields.io/badge/version-2.1.0-brightgreen.svg)

### <a name=“REPO”></a>1.添加代码仓库

**项目根目录的build.gradle文件中，添加**

```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven {url "http://developer.seedland.cc/nexus/repository/maven-public/"}
        // maven {url "http://developer.seedland.cc/nexus/repository/maven-dev/"} // 测试环境联调时使用
    }
}
```
### <a name="DEPENENCY"></a>2.添加依赖

**在App的build.gradle文件的dependencies块中，添加**

```gradle
dependencies {
    implementation('cc.seedland.inf:passport:2.1.0@aar'){
            transitive=true
    }
}
```

### <a name="SUPPORT"></a>3.添加开发者支持

**获取开发者channel、开发者key，提供用户协议url**

（wiki）
用户协议url在注册界面中使用，申请channel和key时，请一起提供，否则用户点击“用户协议”链接时，将***<mark>不能正常打开用户协议界面</mark>***

**将开发者channel及key到工程**

在App目录下的gradle.build文件中：

* 在defaultConfig统一添加

```gradle

defaultConfig {
        // channel和key为资源名，固定使用以下名称
        resValue "string", "channel", "your channel"      // SDK的渠道号
        resValue "string", "key", "your key"              // SDK的开发者Key
}
```

### <a name="PROGUARD"></a>4.混淆规则
1. 在app的proguard-rules.pro文件中添加如下规则
2. 如果项目中已经添加okhttp和Gson的混淆规则，直接添加# passport部分的混淆规则即可

```
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Appliction

-ignorewarnings
-keep class **.R$* {*;}

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}

# passport
-keepattributes InnerClasses
-keepparameternames
-keep class cc.seedland.inf.passport.PassportHome{*;}
-keepparameternames
-keep class cc.seedland.inf.passport.stat.**{*;}
-keep class cc.seedland.inf.passport.stat.PassportStatAgent$Builder{*;}
-keep public class * extends cc.seedland.inf.network.BaseBean{*;}
-keep class cc.seedland.inf.network.BaseBean{*;}
-keep class cc.seedland.inf.network.BeanWrapper{*;}
-keepparameternames
-keep interface cc.seedland.inf.passport.network.TokenCallback{*;}

```




## <a name="DOC"></a>文档

### <a name="INITIAL"></a>1. 初始化

为工程创建自定义Application，并执行SDK的初始化方法

```java
public class SampleApplication extends Application {

    ...

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        PassportHome.init(this);
        
        ...
    }
}
```

在AndroidManifest.xml文件中，使用自定义的Application，如

```xml
<application
        android:name=".SampleApplication"
        ...
</application>
```

### <a name="UPDATE-TOKEN"></a>2. 开启自动刷新token
开启自动刷新token功能后，在每次打开app时会自动刷新token，默认为关闭


***注意：必须在初始化后调用，否则不起作用，建议在Application中调用***

``` java
PassportHome.enableTokenUpdate(true);
```


### <a name="CALL"></a>3. 调用

SDK统一使用startActivityForResult的方式调用支持Passport APIs的界面，包括以下步骤：

1. 定义RequestCode
2. 启动活动
3. 在onActivityResult回调中接收和处理返回结果

    * 解析结果：通过data.getBundleExtra("result")获得，以键值对形式提供，保存在Bundle对象中
    * 原始结果：通过data.getStringExtra("raw_result")获得，为API返回的json数据，供用户自行解析

以密码登录界面为例

1. 定义请求码

```java
...
private static final int REQUEST_CODE_LOGIN = 1; // 可以是任意整型数字
...
```

2. 启动活动

```java
...
// this为Activity的实例
PassportHome.getInstance().startLoginByPassword(this, REQUEST_CODE_LOGIN);
...
```

3. 接收和处理结果

执行操作返回码为*RESULT_OK*，未执行操作返回码为*RESULT_CANCELED*


```java
...

执行操作返回码为RESULT_OK，未执行操作返回码为RESULT_CANCELED

@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_LOGIN:
                    Bundle args = data.getBundleExtra("result");
                    String uid = args.getString("uid");
                    String nickname = args.getString("nickname");
                    String mobile = args.getString("mobile");
                    String rawResult = data.getStringExtra("raw_result");
                    ...
                    break;
               ...
            }
        }else if(resultCode == RESULT_CANCELED) {
            switch(requestCode) {
                case REQUEST_CODE_LOGIN:
                    ....
                    break;
            }
            ...
        }
        ...
    }
...
```

**<a name="CHECK-LOGIN"></a>检查登录状态**

* 调用方法：

```java

PassportHome.getInstance().checkLogin(new TokenCallback(){
            
            @Override
            public void onTokenReceived(String newToken) {
                // 刷新成功 - 处于登录状态
                ...
            }

            @Override
            public void onTokenExpired(String oldToken) {
                // 刷新失败 - 登录过期
                ...
            }
        });

```
* 返回内容
	* 检验成功，执行onTokenReceived(String newToken), 获取新token
	* 检验失败，执行onTokenExpired(String oldToken), 返回旧token

**<a name="REGISTER"></a>注册**

* 调用方法：PassportHome.getInstance().startRegister(this, REQUEST_CODE_REGISTER);
* 返回内容
    * 解析数据  
        * uid
        * mobile
        * nickname
        * ip          
    * 原始数据

```json

{
        "error_code":0,
        "error_message":"succeed",
        "data":{
            "uid":99,
            "mobile":"13800138000",
            "nickname":"User_13800138000",
            "register_ip":"192.168.232.2",
            "sso_tk":"WCATe8Iy4itr5BSNIQPjPS/Bg/obSS7caEPuAREb5zeDkuFacEv0H8UxK9QsWPXoo79O+Ei50tJkWd+AGtQa3Vnv55Zt8QHCoygruuR+egmQELHCYCJK4KVU8H75FfkP4Lt6dZfTaQmiiX7lcU3qP1eN+V+/5GQRK7H3posS96I=",
            "register_time":1511335462
        },
        "sign":"FR9YiHFoSwodTI4h3oQfobd8FLWvPFKPNPq+23dcvLnXvKSRpuk0Z6nkvR0jgs2R1PMD+4hAUz3nTiyrY8+7ntxw+TDXKNqib6CGrcS1WAAAPUJTtknVjojM+hz+MabNMugY5k6rPd9bktCwr1TxlXSBQZPMxPrXVsTp86gQt1g=",
        "timestamp":1513677449
    }
    
```

**<a name="LOGIN-PASSWORD"></a>密码登录**

* 调用方法：PassportHome.getInstance().startLoginByPassword(this, REQUEST_CODE_LOGIN);
* 返回内容
    * 解析数据
        * uid
        * mobile
        * nickname
        * ip
        * token
    * 原始数据  

```json

    {
        "error_code":0,
        "error_message":"succeed",
        "data":{
            "uid":100,
            "mobile":"13800138000",
            "nickname":"User_13800138000",
            "register_ip":"47.92.127.31",
            "sso_tk":"WCATe8Iy4itr5BSNIQPjPS/Bg/obSS7caEPuAREb5zeDkuFacEv0H8UxK9QsWPXoo79O+Ei50tJkWd+AGtQa3Vnv55Zt8QHCoygruuR+egmQELHCYCJK4KVU8H75FfkP4Lt6dZfTaQmiiX7lcU3qP1eN+V+/5GQRK7H3posS96I=",
            "register_time":1511335462
        },
        "sign":"FR9YiHFoSwodTI4h3oQfobd8FLWvPFKPNPq+23dcvLnXvKSRpuk0Z6nkvR0jgs2R1PMD+4hAUz3nTiyrY8+7ntxw+TDXKNqib6CGrcS1WAAAPUJTtknVjojM+hz+MabNMugY5k6rPd9bktCwr1TxlXSBQZPMxPrXVsTp86gQt1g=",
        "timestamp":1513677449
    }
    
```

**<a name="LOGIN-CAPTCHA"></a>验证码登录**

* 调用方法：PassportHome.getInstance().startLoginByCaptcha(this, REQUEST_CODE_LOGIN_CAPTCHA);
* 返回内容
    * 解析数据 
        * uid
        * mobile
        * nickname
        * ip
        * token       
    * 原始数据 

```json
{
        "error_code":0,
        "error_message":"succeed",
        "data":{
            "uid":100,
            "mobile":"13800138000",
            "nickname":"User_13800138000",
            "register_ip":"47.92.127.31",
            "sso_tk":"WCATe8Iy4itr5BSNIQPjPS/Bg/obSS7caEPuAREb5zeDkuFacEv0H8UxK9QsWPXoo79O+Ei50tJkWd+AGtQa3Vnv55Zt8QHCoygruuR+egmQELHCYCJK4KVU8H75FfkP4Lt6dZfTaQmiiX7lcU3qP1eN+V+/5GQRK7H3posS96I=",
            "register_time":1511335462
        },
        "sign":"FR9YiHFoSwodTI4h3oQfobd8FLWvPFKPNPq+23dcvLnXvKSRpuk0Z6nkvR0jgs2R1PMD+4hAUz3nTiyrY8+7ntxw+TDXKNqib6CGrcS1WAAAPUJTtknVjojM+hz+MabNMugY5k6rPd9bktCwr1TxlXSBQZPMxPrXVsTp86gQt1g=",
        "timestamp":1513677449
    }
```

**<a name="PASSWORD-RESET"></a>重置（忘记）密码**

* 调用方法：PassportHome.getInstance().startResetPassword(this, REQUEST_CODE_RESET_PASSWORD);
* 返回内容
    * 解析数据
        * uid
        * mobile
        * nickname
        * ip    
    * 原始数据 

```json
{
    "error_code":0,
    "error_message":"succeed",
    "data":{
        "uid":100,
        "mobile":"13800138000",
        "nickname":"User_13800138000",
        "register_ip":"47.92.127.31",
        "register_time":1511335462
    },
    "sign":"rVbOKhaY5/0o512Ukm8dcSF1LIIupM6Y6weZ1XnJFQdk5BcNOj7p9MyeFxex9GMAsXgmw1qdpeMAlVv7j1OgDwkIRRnmFyGW6WQM3tRQDsFiQgowpVZixwauBjVS7Fo2JDg60uq/KPyrnN074qCfQOAILdMlcWdPXpYGfyyLlbs=",
    "timestamp":1513678877
}
```
**<a name="PASSWORD-MODIFY"></a>修改密码**

* 调用方法：PassportHome.getInstance().startModifyPassword(this, REQUEST_CODE_PASSWORD);
* 返回内容-修改后登录
    * 解析数据
        * uid
        * mobile
        * nickname
        * ip
        * token
    * 原始数据  

```json
{
    "error_code":0,
    "error_message":"succeed",
    "data":{
        "uid":100,
        "mobile":"13800138000",
        "nickname":"User_13800138000",
        "register_ip":"47.92.127.31",
        "sso_tk":"VmRhKOnoYOUAg7ZyVY69GIQ5M9+iBty0k+uKYDQo9JbAYTP5hsVLMJNDClCXGZFL1CzR+yKzzKIbTpbJLNTK7TjwbsrO/IK8S/+79h8bEAWUOku6/GUgbJxRkjYp4i1KfsbV2BRuvNh7EqCBFnMbKFjhK6GiuGqvIId8ymgk1vU=",
        "register_time":1511335462
    },
    "sign":"nK+mDGgNA9nL41g0EGfP7OXM8qOwFDI2Pz3hX8IrRsIzhcsQBuLsZZy6kX/xgOq/E0p5/feXNtQ4CdJ5Z5Y+8kS2ebv7bVgx4+5NgLxzGO7xmeG22bypfWg1yysjgvsL8FvZGe2IsYg5KwQZCprMYK6rseQU48cgOGCF+OfQuyE=",
    "timestamp":1513679274
}
```

**<a name="TOKEN"></a>获取Token**

_调用方请使用SDK提供的获取Token的方式_

调用方法：

```java
...
String token = PassportHome.getInstance().getToken();
...
```

**<a name="LOGOUT"></a>登出**

_当APP退出登录状态时调用_

调用方法:

```java
...
PassportHome.getInstance().logout();
...
```