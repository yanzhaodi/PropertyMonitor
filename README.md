# PropertyMonitor

# 给变量改变前后添加监听事件
主要采用的是java.beans.PropertyChangeSupport来现实变量的监听
通过apt的方法将主要代码通过自动生成代码的方式

如何使用apt以及最终将库发布到jCenter，请参考我之前的一片文章：
http://www.jianshu.com/p/1502674152bd

## 集成方法

```
顶层build.gradle配置：
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'

由于没有发布到jCenter，所以需要加上下面的maven地址：
repositories {
    mavenCentral();
    jcenter()

    maven{
        url 'https://dl.bintray.com/yanzhaodi/maven'
    }
}


主工程build.gradle配置：
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'com.yzd:propertymonitor:1.0.0'
    apt 'com.yzd:propertymonitor-compiler:1.0.0'
}
```

使用：

```
public class MainActivity extends Activity {

    // willSet指定的方法在变量改变前调用，didSet指定的在变量改变后调用
    // 不设置表示不作处理
    @Monitor(willSet = "nameChangeBefore", didSet = "nameChangeAfter")
    String name = "李四";

    // 返回值含义，true：不执行改变变量的命令，false：执行改变变量的命令
    boolean nameChangeBefore() {
        Log.d("MainActivity", "nameChangeBefore");
        return true;
    }

    // 变量改变后的监听方法有两个参数，类型为监听对象的类型，第一个是旧值，第二个是新值
    void nameChangeAfter(String oldValue, final String newValue) {
        Log.d("MainActivity", "nameChangeAfter");
        // 回调发生在非UI线程
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                helloTv.setText(newValue);
            }
        });
    }

    TextView helloTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helloTv = (TextView) findViewById(R.id.tv_hello);

        // 会为每个有被Monitor注解修饰的变量的类创建一个Helper类，类名为当前类名加上Helper
        // 在每个类初始化的地方调用Helper的init方法，可以得到一个Helper的实例
        // 如果想触发变量改变的监听，必须调用helper为该变量提供的set方法。
        final MainActivityHelper helper = (MainActivityHelper) MainActivityHelper.init(this);

        new Thread() {
            @Override
            public void run() {
                int a = 10;
                while (true) {
                    a++;

                    // 必须调用Helper的set方法才能成功监听
                    helper.setName("张三" + a);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }
}

```