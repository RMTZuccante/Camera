#include "ImageProcessor.h"

ImageProcessor::ImageProcessor() {
    if (DEBUG_WINDOW) cv::namedWindow("ImageProcessor");
}

std::vector<cv::Mat> &ImageProcessor::process(cv::Mat &image) {
    digits.clear();
    img = image;

    cvtColor(img, imgGray, CV_BGR2GRAY); // convert to gray

    rotate(detectSkew()); // detect and correct remaining skew (+- 30 deg)

    findCharacters(); // find and isolate counter digits

    if (DEBUG_WINDOW) cv::imshow("ImageProcessor", img);
    return digits;
}

void ImageProcessor::rotate(double rotationDegrees) {
    cv::Mat M = cv::getRotationMatrix2D(cv::Point(imgGray.cols / 2, imgGray.rows / 2), rotationDegrees, 1);
    cv::Mat img_rotated;
    cv::warpAffine(imgGray, img_rotated, M, imgGray.size());
    imgGray = img_rotated;
    if (DEBUG_WINDOW) {
        cv::warpAffine(img, img_rotated, M, img.size());
        img = img_rotated;
    }
}

void ImageProcessor::drawLines(std::vector<cv::Vec2f> &lines) {
    // draw lines
    for (auto &i : lines) {
        float rho = i[0];
        float theta = i[1];
        double a = cos(theta), b = sin(theta);
        double x0 = a * rho, y0 = b * rho;
        cv::Point pt1(cvRound(x0 + 1000 * (-b)), cvRound(y0 + 1000 * (a)));
        cv::Point pt2(cvRound(x0 - 1000 * (-b)), cvRound(y0 - 1000 * (a)));
        cv::line(img, pt1, pt2, cv::Scalar(255, 0, 0), 1);
    }
}

float ImageProcessor::detectSkew() {
    cv::Mat edges;

    //Detect edges using Canny algorithm.
    cv::Canny(imgGray, edges, CANNY_1, CANNY_2);

    // find lines
    std::vector<cv::Vec2f> lines;
    cv::HoughLines(edges, lines, 1, CV_PI / 180.f, 140);

    // filter lines by theta and compute average
    std::vector<cv::Vec2f> filteredLines;
    float theta_min = 60.f * CV_PI / 180.f;
    float theta_max = 120.f * CV_PI / 180.0f;
    float theta_avr = 0.f;
    float theta_deg = 0.f;
    for (auto &line : lines) {
        float theta = line[1];
        if (theta >= theta_min && theta <= theta_max) {
            filteredLines.push_back(line);
            theta_avr += theta;
        }
    }
    if (!filteredLines.empty()) {
        theta_avr /= filteredLines.size();
        theta_deg = (theta_avr / CV_PI * 180.f) - 90;
    }

    if (DEBUG_SKEW) drawLines(filteredLines);

    return theta_deg;
}

void
ImageProcessor::filterContours(std::vector<std::vector<cv::Point> > &contours, std::vector<cv::Rect> &boundingBoxes,
                               std::vector<std::vector<cv::Point> > &filteredContours) {
    for (const auto &contour : contours) {
        cv::Rect bounds = cv::boundingRect(contour);
        if (bounds.height > DIG_MIN && bounds.height < DIG_MAX && bounds.width > 5 && bounds.width < bounds.height) {
            boundingBoxes.push_back(bounds);
            filteredContours.push_back(contour);
        }
    }
}

void ImageProcessor::findCharacters() {
    // edge image
    cv::Mat edges;
    cv::Canny(imgGray, edges, CANNY_1, CANNY_2);

    cv::Mat img_ret = edges.clone();

    // find contours in whole image
    std::vector<std::vector<cv::Point> > contours, filteredContours;
    std::vector<cv::Rect> boundingBoxes;
    cv::findContours(edges, contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE);

    // filter contours by bounding rect size
    filterContours(contours, boundingBoxes, filteredContours);

    if (DEBUG_EDGES) {
        cv::imshow("edges", edges);
        // draw contours
        cv::Mat cont = cv::Mat::zeros(edges.rows, edges.cols, CV_8UC1);
        cv::drawContours(cont, filteredContours, -1, cv::Scalar(255));
        cv::imshow("contours", cont);
    }

    // cut out found rectangles from edged image
    for (const auto &roi : boundingBoxes) {
        digits.push_back(img_ret(roi));
        if (DEBUG_DIGITS) cv::rectangle(img, roi, cv::Scalar(0, 255, 0), 2);
    }
}
