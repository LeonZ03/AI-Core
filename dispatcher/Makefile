BUILD_DIR = ../core/cvxif_example
TEST_NAME ?= ADD.Adder_Test
SV_NAME ?= Adder_sv


compile:
	mill -i myproject.compile | pv -t

testAll:
	mill -i myproject.test.test | pv -t

testOnly:
	mill -i myproject.test.testOnly ${TEST_NAME} | pv -t

sv:
	mkdir -p $(BUILD_DIR)
	mill -i myproject.runMain ${SV_NAME} --target-dir $(BUILD_DIR) | pv -t

clean:
	rm -rf ./out

install_tools:
	sudo apt update && sudo apt install -y \
		openjdk-17-jdk \
		python3 \
		python3-pip \
		make \
		git \
		curl \
		pv \
		cloc \
		verilator

	# 安装 mill 0.11.1
	curl -L https://github.com/com-lihaoyi/mill/releases/download/0.11.1/0.11.1 > mill && \
	chmod +x mill && \
	sudo mv mill /usr/local/bin/
	@echo "✅ 当前mill版本已为0.11.1"

	# 可选：安装常用 Python 库（用于测试、绘图、数据处理）
	pip3 install --user -U matplotlib numpy wheel

	@echo "✅ All tools have been installed!"
