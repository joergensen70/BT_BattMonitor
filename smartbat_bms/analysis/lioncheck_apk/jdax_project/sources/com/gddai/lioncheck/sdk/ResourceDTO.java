package com.gddai.lioncheck.sdk;

/* JADX INFO: loaded from: classes.dex */
public class ResourceDTO extends MessageDTO {
    private static final long serialVersionUID = -624084162733857157L;
    private ResourceBusiness business;
    private Long id;
    private Long relId;
    private String resUrl;
    private ResourceType type;

    public String getResUrl() {
        return this.resUrl;
    }

    public void setResUrl(String str) {
        this.resUrl = str;
    }

    public Long getRelId() {
        return this.relId;
    }

    public void setRelId(Long l) {
        this.relId = l;
    }

    public ResourceBusiness getBusiness() {
        return this.business;
    }

    public void setBusiness(ResourceBusiness resourceBusiness) {
        this.business = resourceBusiness;
    }

    public ResourceType getType() {
        return this.type;
    }

    public void setType(ResourceType resourceType) {
        this.type = resourceType;
    }

    @Override // com.gddai.lioncheck.sdk.MessageDTO
    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
    }
}
