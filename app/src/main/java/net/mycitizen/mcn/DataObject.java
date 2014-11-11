package net.mycitizen.mcn;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public abstract class DataObject {
    protected String objectType;
    protected int objectId;

    protected ArrayList<DataObject> tags;

    public DataObject(int id, String type) {
        this.objectId = id;
        this.objectType = type;
    }

    public String getObjectType() {
        return objectType;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setTags(ArrayList<DataObject> tags) {
        this.tags = tags;
    }

    public ArrayList<DataObject> getTags() {

        return this.tags;
    }

    public ArrayList<DataObject> getUsers(ApiConnector api) {
        // todo load all if viewing friends/connections
        ArrayList<DataObject> users = api.getData("user", "filter[" + this.objectType + "_id" + "]=" + this.objectId, true, null);

        return users;
    }

    public ArrayList<DataObject> getGroups(ApiConnector api) {
        // todo load all if viewing friends/connections
        ArrayList<DataObject> groups = api.getData("group", "filter[" + this.objectType + "_id" + "]=" + this.objectId, true, null);

        return groups;
    }

    public ArrayList<DataObject> getResources(ApiConnector api) {
        // todo load all if viewing friends/connections
        ArrayList<DataObject> resources = api.getData("resource", "filter[" + this.objectType + "_id" + "]=" + this.objectId + "&filter[type][0]=2&filter[type][1]=3&filter[type][2]=4&filter[type][3]=5&filter[type][5]=6", true, null);

        return resources;
    }

    public abstract LinkedHashMap<String, String> getAllData();


}
