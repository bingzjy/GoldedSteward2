package com.ldnet.entities;

import java.io.Serializable;

/**
 * Created by lee on 2016/7/27.
 */
public class MessageData implements Serializable {

    protected String Title;
    protected String Content;
    protected String Created;
    protected String Type;//0-物业消息
    protected String Id;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getCreated() {
        return Created;
    }

    public void setCreated(String created) {
        Created = created;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
