package net.mycitizen.mcn;

import java.util.LinkedHashMap;

public class TagObject extends DataObject {

    private String title;
    private int tag_parent_id;
    private boolean status;

    public TagObject(int id, String title) {
        super(id, "tag");
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public LinkedHashMap<String, String> getAllData() {
        LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

        data.put("id", String.valueOf(this.objectId));
        data.put("title", String.valueOf(this.objectId));

        return data;
    }


    public int getTag_parent_id() {
        return tag_parent_id;
    }

    public void setTag_parent_id(int tag_parent_id) {
        if (tag_parent_id != 0) {
            this.title = "↳ " + this.title;
        }
        this.tag_parent_id = tag_parent_id;
    }
}
