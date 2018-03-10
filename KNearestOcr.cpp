#include "KNearestOcr.h"

KNearestOcr::KNearestOcr() {
    model = cv::ml::KNearest::create();
}

int KNearestOcr::learn(const cv::Mat &img) {
    cv::imshow("Learn", img);
    int key = cv::waitKey(0);
    if (key == 'u' || key == 'h' || key == 's') {
        responses.push_back(cv::Mat(1, 1, CV_32F, (float) key));
        samples.push_back(prepareSample(img));
    }
    return key;
}

int KNearestOcr::learn(const std::vector<cv::Mat> &images) {
    for (auto &img : images) {
        int key = learn(img);
        if (key == 'f' || key == 'q') return key;
    }
    return 0;
}

char KNearestOcr::recognize(const cv::Mat &img) {
    char cres = '?';
    cv::Mat results, neighborResponses, dists;
    float result = model->findNearest(prepareSample(img), 2, results, neighborResponses, dists);
    if (0 == int(neighborResponses.at<float>(0, 0) - neighborResponses.at<float>(0, 1))
        && dists.at<float>(0, 0) < DIST_MAX) {
        // valid character if both neighbors have the same value and distance is below ocrMaxDist
        cres = (int) result;
    }
    return cres;
}

std::string KNearestOcr::recognize(const std::vector<cv::Mat> &images) {
    std::string result;
    for (const auto &image : images) {
        char res = recognize(image);
        if (res != '?') result += res;
    }
    return result;
}

void KNearestOcr::save() {
    cv::FileStorage fs(TRAINDATA, cv::FileStorage::WRITE);
    fs << "samples" << samples;
    fs << "responses" << responses;
    fs.release();
}

bool KNearestOcr::load() {
    cv::FileStorage fs(TRAINDATA, cv::FileStorage::READ);
    if (!fs.isOpened()) return false;
    fs["samples"] >> samples;
    fs["responses"] >> responses;
    fs.release();
    model->train(samples, cv::ml::ROW_SAMPLE, responses);
    return true;
}

cv::Mat KNearestOcr::prepareSample(const cv::Mat &img) {
    cv::Mat roi, sample;
    cv::resize(img, roi, cv::Size(10, 10));
    roi.reshape(1, 1).convertTo(sample, CV_32F);
    return sample;
}
