# plato
编排框架，后续支持微服务级别编排。

1、去掉边的概念，只通过next指针连接。
2、合并聚合器和转换器，采用preHandler和afterHandler代替。
3、更少的客户端代码交互，并且交互代码易懂。
4、yml方式提供方法级别的node、方法级别的preHandler、方法级别的afterHandler。防止业务流程过多导致的类爆炸情况。
5、关于逸飞提出的性能问题，框架整体基于CompletableFuture来实现的。会让程序运行的更快。
6、关于启明提出的在运行过程中。某个数据不知道是从哪个节点透传过来的。解决办法是将一个图的traceId维度运行结果返回给用户，让用户自行装配。
7、退化GraphId的概念。关于图的概念。框架只保留下graphId和graphTraceId两个变量。
8、增加强关联，弱关联概念。可以在保证图的结构定义不变的情况下。组合出多种运行路径。
9、Node不在局限在类的形式，可以是方法、EL表达式、子流程、bean形式。如果框架的到推广，甚至可以将Node定义为一个rpc服务级别的方法调用。
10、判断节点是否能够执行，在两个地方判断。1、前置节点执行之后。2、增加节点自杀功能。
11、整个框架结构简单。代码编排方式甚至都可以做到脱离Spring运行。
12、框架依赖外部jar包仅仅包含几个常用类似guava的工具包。
13、在并行执行时，前置节点可以观测后置节点是否已完成，来判断是否要执行。
14、在运行之后对GraphHolder中的当前graphTraceId生命周期数据进行倾倒。
15、在加载yml文件的时候，是通过SPI机制实现的。当某一个引用jar包的工程在启动的时候，会调用我的启动监听器。这时进行yml文件的加载。
16、所有的节点定义都是单例的，也就是在服务启动只会加载一次所有节点的定义。动态执行的代理对象。
17、为了让Node复用,去掉了将运行时数据放到回调函数中。但是还要在执行时获取运行时数据。我们将运行时数据放到preHandler和afterHandler中。
18、子流程的功能，可以减少客户端定义Node的重复流程。开发更加高效。

目前问题
1、目前参数有问题。
2、异常是否继续执行功能。
3、目前yml只支持方法级别Node。preHandler和afterHandler都是方法级别。需要支持多种级别。
4、目前代码编排方式只支持Bean级别。需要支持方法级别

后续丰富内容
1、增加事务功能
2、后续扩展，考虑增加RPC级别Node调用
3、使用Jmeter进行压力测试，对比数据。

思路想法
1、平滑热更新定义Node编排结构（刚需）。重点问题在程序运行时，如何保证定义的修改。
2、添加动态线程池功能（目前正在编写中。。）
3、集合Echarts