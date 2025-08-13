package platform.purchase;

import platform.entity.PurchaseData;

interface PurchaseInterface {

   void init();

   boolean settlePurchase(PurchaseData data);

   boolean containsProduct(PurchaseData data);


}
