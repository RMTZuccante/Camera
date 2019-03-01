How to build opencv on raspberry pi 3 for java and python3:
download and unzip opencv source code version 3.4.5
for dependencies follow this guide: https://www.pyimagesearch.com/2017/09/04/raspbian-stretch-install-opencv-3-python-on-your-raspberry-pi/
after installing python3-dev go to step "Installing NumPy on your Raspberry Pi"
never use workon or virtualenvs
run sudo apt install openjdk-8-jdk
run export JAVA_HOME='/usr/lib/jvm/java-8-openjdk-armhf'
run export PATH=$JAVA_HOME/bin:$PATH
run sudo apt install ant
make sure to be in /opencv/build
run cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D WITH_FFMPEG=OFF -D BUILD_EXAMPLES=OFF ..
make sure Python 3 interpreter, libraries etc are setted
make sure java ant, JNI, wrappers are setted
follow the guide to increment swap size
finally run make -j4
