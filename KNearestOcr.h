#ifndef KNEARESTOCR_H
#define KNEARESTOCR_H

#include <vector>
#include <string>
#include <opencv2/ml/ml.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include "Config.h"

class KNearestOcr {
public:
    KNearestOcr();

    int learn(const cv::Mat &img);

    int learn(const std::vector<cv::Mat> &images);

    char recognize(const cv::Mat &img);

    std::string recognize(const std::vector<cv::Mat> &images);

    void save();

    bool load();

private:
    cv::Mat prepareSample(const cv::Mat &img);

    cv::Mat samples;
    cv::Mat responses;
    cv::Ptr<cv::ml::KNearest> model;
};

#endif
