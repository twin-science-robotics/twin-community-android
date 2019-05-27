package com.twinscience.twin.lite.android.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.squareup.moshi.Json;
import com.twinscience.twin.lite.android.project.def.ProjectDef;


import java.util.UUID;


public class ProjectModel implements Parcelable {


    @Json(name = "id")
    private String id;

    private ProjectDef type;

    @Json(name = "title")
    private String title;

    @Json(name = "date")
    private String date;

    @Json(name = "image")
    private String image;

    @Json(name = "langCode")
    private String langCode;

    private String fileName;

    public ProjectModel(String id, ProjectDef type, String title, String date, String image, String langCode, String fileName) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.date = date;
        this.image = image;
        this.langCode = langCode;
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProjectDef getType() {
        return type == null ? ProjectDef.TWIN : type;
    }

    public void setType(ProjectDef type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getFileName() {
        switch (getType()) {
            case NEW:
                return UUID.randomUUID().toString();
            case PERSONAL:
                return fileName;
            default:
                return id.toLowerCase().replace("-", "") + ".xml";
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.title);
        dest.writeString(this.date);
        dest.writeString(this.image);
        dest.writeString(this.langCode);
        dest.writeString(this.fileName);
    }

    protected ProjectModel(Parcel in) {
        this.id = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : ProjectDef.values()[tmpType];
        this.title = in.readString();
        this.date = in.readString();
        this.image = in.readString();
        this.langCode = in.readString();
        this.fileName = in.readString();
    }

    public static final Creator<ProjectModel> CREATOR = new Creator<ProjectModel>() {
        @Override
        public ProjectModel createFromParcel(Parcel source) {
            return new ProjectModel(source);
        }

        @Override
        public ProjectModel[] newArray(int size) {
            return new ProjectModel[size];
        }
    };
}