# spring-datamark

一款基于 Spring 框架的数据标记处理器。

## 特性

- 能够轻松地通过配置获取一个数据标记处理器，以获取当前的数据标记的值。
- 数据标记基于 Spring Resource 进行加载。
- 提供标记刷新 API，可以重复读取 Spring Resource，并刷新数据标记。
- 提供标记更新 API，当 Spring Resource 支持写入时，可以更新数据标记。
- 使用读写锁线程安全的同时提高并发效率。
