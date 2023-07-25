# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.8/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.8/maven-plugin/reference/html/#build-image)

### 部署 ###

* freeswitch部署位置(10.194.31.200/202):
  /usr/local/xswtich
* xcc部署位置:(10.194.31.201/203):
  /home/app/chryl/chryl-xcc.jar
* 发布命令:
  nohup java -jar chryl-xcc.jar &

### 如何验证xcc部署成功 ###

* 查看配置是否加载成功(10.194.31.114):
  IP:8088/xcc/chryl

### 日志 ###

* xcc日志日志位置(日志文件保留30天):
  /home/app/chryl/logs/xcc
* 查看udp端口:
  nc -zvu IP PORT
* 抓包
  tcpdump -i any -w /tmp/111.pcap
* 监控话务(10.194.31.200/202):
  sngrep

### FreeSwitch ###

* 此部分需要进入运行中断:
  cd /usr/local/xswtich
  make cli
* 开启/关闭日志:
  sofia global siptrace on/off
* 重置freeswitch.log:
  fsctl send_sighup
* 重载xcc程序(需设置自动加载xcc)
  show modules mod_xcc
  reload mod_xcc
* 查看unimrcp
  show modules mod_unimrcp
* 查看注册话机
  show registrations as xml

### Docker ###

* freeswitch使用docker部署
  docker exec -it xswitch /bin/bash
* freeswitch-log位置
  /usr/local/freeswitch/log/freeswitch.log
* 拷贝freeswitch日志到宿主机,这里使用/tmp/chryl为例
  docker cp xswitch:/usr/local/freeswitch/log/freeswitch.log /tmp/chryl/

### XSwitch管理平台 ###

* 转接
  info
  bridge sofia/default/4001@10.194.31.92:5060

* 模拟环回
  info
  ring_ready
  sleep 1000
  answer
  echo


* xcc应答
  answer
  xcc

* 播报语音文件,需要将语音嗯就放到容器
  answer
  detect_speech unimrcp
  {start-input-timers=true,Speech-Complete-Timeout=1000,no-input-timeout=3000,recognition-timeout=60000}builtin:
  grammar/boolean?language=zh-CN;y=1;n=2 builtin
  playback /tmp/news0.wav

* 播报文本
  answer
  speak 早上好！欢迎到中山来，很高兴在中山见到你们，谢谢！这么多兄弟姐妹来到中山，我和牛新哲主席一直在谈到底我们用什么来欢迎大家，我想至少有三个理由。

* SIP转接
  originate [origination_caller_id_number=9000]sofia/default/4001@10.194.31.200:5060 &echo

