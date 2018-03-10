#include "Camera.h"

int main() {
    Camera cam(0);

    std::cout << "<l> learn, <t> test, <a> adjust" << std::endl;

    char cmd;
    std::cin >> cmd;

    switch (cmd) {
        case 'l':
            cam.learn();
            break;
        case 't':
            cam.test();
            break;
        case 'a':
            cam.adjust();
            break;
    }

    return 0;
}
