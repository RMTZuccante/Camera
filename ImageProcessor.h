#ifndef IMAGEPROCESSOR_H
#define IMAGEPROCESSOR_H

#include <vector>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include "Config.h"


class ImageProcessor {
public:
    ImageProcessor();

    std::vector<cv::Mat> &process(cv::Mat &image);

private:
    void rotate(double rotationDegrees);

    void findCharacters();

    float detectSkew();

    void drawLines(std::vector<cv::Vec2f> &lines);

    void filterContours(std::vector<std::vector<cv::Point> > &contours, std::vector<cv::Rect> &boundingBoxes,
                        std::vector<std::vector<cv::Point> > &filteredContours);

    cv::Mat img;
    cv::Mat imgGray;
    std::vector<cv::Mat> digits;
};

#endif
