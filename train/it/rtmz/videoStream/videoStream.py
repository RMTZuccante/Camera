import cv2
import base64
import zmq

if __name__ == "__main__":
    camera = cv2.VideoCapture(0)  # Open first camera available

    # Configure socket
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind(('localhost', 1026))
    server.listen(1)  # Limit to 1 connection

    client, addr = server.accept()  # Wait for a connection
    print('Connection accepted')

    while 1:
        _, frame = camera.read()
        client.sendall(pickle.dumps(frame))
        cv2.imshow("Ciao", frame)
        cv2.waitKey(1)
