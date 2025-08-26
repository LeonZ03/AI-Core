# chisel工程模板

本项目旨在帮助使用mill构建chisel项目的开发者快速搭建环境，包括符合格式的工程目录和需要的所有工具链

工程内包含一个简单的加法器，可作为样例供开发者参考

## 开发环境说明

本项目基于 Ubuntu 系统开发与测试，推荐使用 Ubuntu 22.04或wsl


| 工具      | 推荐版本         |
| --------- | ---------------- |
| Java      | OpenJDK 17       |
| Mill      | 0.11.1           |
| verilator | 默认即可         |
| Python    | ≥ 3.10          |
| pip       | ≥ 22.0          |
| git/make  | 默认即可         |
| pv        | 用于终端可视化   |
| cloc      | 用于统计代码行数 |

可使用make命令 `make install_tools` 一键安装所有工具

在配置开发环境过程中，可能需要访问github，建议使用vpn全局代理，并通过ssh密钥访问

## Quick start

```
make compile  #编译工程（可选，可用于验证是否成功配置开发环境）
```

## Generate SystemVerilog

```
make sv SV_NAME=xxx_sv  #specific target , eg: SV_NAME=Adder_sv
```

## ChiselTest

```
make testAll  #run all tests 
make testOnly TEST_NAME=packageName.testName  #specific target , eg: TEST_NAME=ADD.Adder_Test
```



## Clean mill cache

```
make clean
```
