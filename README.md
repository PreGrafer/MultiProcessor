文件说明:
MultiProcessor.java 源代码
MultiProcessor.jar 可执行文件
Commands.txt 指令文件(与可执行文件在同一文件夹下即可执行)
程序迭代日志.png Git版本管理记录
演示视频.mp4 程序演示视频

程序说明:
本程序编译与执行环境JDK17.0.9(经测试JDK1.8.381下可运行 界面可能有变形)
使用IntelliJ IDEA编写(附Git版本记录)
本模拟器采用直接映射和写回法

指令说明(特指Commands.txt):

w 处理器ID 主存地址 处理器X读主存地址
w a 31 处理器a读主存第31块

r 处理器ID 主存地址 数据 处理器X为主存地址写数据
r a 1 aaa 处理器a为主存第1块写数据aaa