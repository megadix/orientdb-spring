package it.megadix.orientdb.spring;


public class OrientDbTransactionObject {
    private OrientDbConnectionHolder conHolder;

    public OrientDbTransactionObject() {
        super();
    }

    public OrientDbConnectionHolder getConnectionHolder() {
        return conHolder;
    }
    
    public void setConnectionHolder(OrientDbConnectionHolder conHolder) {
        this.conHolder = conHolder;
    }
}
