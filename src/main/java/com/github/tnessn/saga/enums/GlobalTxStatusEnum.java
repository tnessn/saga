package com.github.tnessn.saga.enums;


public enum GlobalTxStatusEnum {

	PROCESSING(1,"处理中"),
    FINISHED(2,"已完成"),
    ERROR(3,"异常"),
    COMPENSATED(4,"已补偿"),    
    ;

    private Integer code;
    private String desc;


    GlobalTxStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static GlobalTxStatusEnum getByCode(Integer code) {
        for (GlobalTxStatusEnum  e: GlobalTxStatusEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
