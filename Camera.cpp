#include "Camera.h"

Camera::Camera(int device, int delay) {
    input.open(device);
    this->delay = delay;
    loaded = ocr.load();
}

void Camera::test() {
    if (loaded) {
        std::cout << "<q> quit" << std::endl;

        cv::Mat img;
        int key = 0;
        while (input.read(img) && key != 'q') {
            std::cout << ocr.recognize(proc.process(img)) << std::endl;
            key = cv::waitKey(delay);
        }

    } else std::cout << "Failed to load training data\n";
}

void Camera::learn() {
    std::cout << "<u>, <h>, <s> to answer digit, <space> ignore, <q> quit" << std::endl;

    cv::Mat img;
    int key = 0;
    while (input.read(img) && !(key == 'q' || key == 'f')) {
        key = ocr.learn(proc.process(img));
    }

    std::cout << "Enter <s> to save, everything else will discard last training data" << std::endl;
    std::cin >> key;
    if (key == 's') {
        std::cout << "Data saved" << std::endl;
        ocr.save();
    }
}

void Camera::adjust() {
    std::cout << "<q> quit" << std::endl;

    cv::Mat img;
    int key = 0;
    while (input.read(img) && key != 'q') {
        proc.process(img);
        key = cv::waitKey(delay);
    }
}

std::string Camera::read() {
    if (!loaded) return "ERR";
    cv::Mat img;
    input.read(img);
    return ocr.recognize(proc.process(img));
}
