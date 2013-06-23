#如何测试
##环境需求
1. 操作系统:

    需要`ubuntu`操作系统
    
2. 安装需要的环境:

```
    sudo apt-get install openjdk-7-jdk ruby1.9.3 iverilog
    gem install rake
```

##测试
1. 将测试源代码及内存数据文件拷贝到项目根目录下，并分别命名为`code.c`和`ram_data.txt`

2. 在项目根目录下执行 `rake` 命令，即可自动编译+模拟并输出结果