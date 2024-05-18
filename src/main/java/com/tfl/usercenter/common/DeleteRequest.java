package com.tfl.usercenter.common;

import lombok.Data;

import java.io.Serializable;

@Data


public class DeleteRequest implements Serializable {

    protected long id;
}
