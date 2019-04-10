package json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Values {
    public int leftCameaId, rightCameraId, thresh, minArea, maxAra, offset, distwall;
    public float bodytemp;
    public String libpath;
    public char[] ref;
    public double precision;
    public int[] paddings;

    private JsonObject obj;

    public boolean load() {
        try {
            leftCameaId = obj.get("CAMERA_LEFT").getAsInt();
            rightCameraId = obj.get("CAMERA_RIGHT").getAsInt();
            JsonArray jsonRef = obj.get("ref").getAsJsonArray();
            ref = new char[jsonRef.size()];
            for (int i = 0; i < ref.length; i++) {
                ref[i] = jsonRef.get(i).getAsCharacter();
            }
            thresh = obj.get("THRESH").getAsInt();
            minArea = obj.get("MIN_AREA").getAsInt();
            maxAra = obj.get("MAX_AREA").getAsInt();
            offset = obj.get("OFFSET").getAsInt();
            precision = obj.get("PRECISION").getAsDouble();
            libpath = obj.get("LIBPATH").getAsString();
            bodytemp = obj.get("BODYTEMP").getAsFloat();
            distwall = obj.get("DISTWALL").getAsInt();
            JsonArray arr = obj.get("FRAME_PADDING").getAsJsonArray();
            paddings = new int[arr.size()];
            for (int i = 0; i < paddings.length; i++) {
                paddings[i] = arr.get(i).getAsInt();
            }
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public Values(JsonObject obj) {
        this.obj = obj;
    }
}
