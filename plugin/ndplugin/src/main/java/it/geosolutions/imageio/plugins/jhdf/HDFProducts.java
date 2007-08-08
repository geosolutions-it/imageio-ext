package it.geosolutions.imageio.plugins.jhdf;

public abstract class HDFProducts {
	private HDFProduct[] productList; 
	private int nProducts;
	
	public HDFProducts(final int nProducts){
		productList=new HDFProduct[nProducts];
		this.nProducts=nProducts;
	}
	
	public void setHDFProduct(final int productIndex, HDFProduct product){
		if(productIndex>nProducts)
			throw new ArrayIndexOutOfBoundsException("Specified product index is out of range");
		else
			productList[productIndex]=product;
	}
	
	public HDFProduct getHDFProduct(final int productIndex){
		if(productIndex>nProducts)
			throw new ArrayIndexOutOfBoundsException("Specified product index is out of range");
		else
			return productList[productIndex];
	}
	
	public HDFProduct getHDFProduct(final String productName){
		final int prodNum = nProducts;
		for (int i=0;i<prodNum;i++){
			final HDFProduct product = productList[i];
			if (product.getProductName().equals(productName))
				return product;
		}
		return null;
	}
	
	public class HDFProduct {
		private String productName;
		private int nBands;
		public HDFProduct(final String productName, final int nBands){
			this.productName=productName;
			this.nBands=nBands;
		}

		public int getNBands() {
			return nBands;
		}

		public String getProductName() {
			return productName;
		}
	}
	
	
	
	
	
}
