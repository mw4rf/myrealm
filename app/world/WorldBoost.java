package world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WorldBoost {

	protected String name;
    protected List<WorldGood> goods = new ArrayList<WorldGood>();

    protected String productName = "";
    protected int productQuantity = 0;
    protected double productRealQuantity = 0;

    protected double realQuantity = 0;

    protected String imagefile;

    protected boolean isBoost = false;

	public WorldBoost(String name) {
		super();
		this.name = name;
	}

	public WorldBoost(String name, List<WorldGood> goods) {
		super();
		this.name = name;
		this.goods = goods;
	}

    public void setImage(String img) {
        this.imagefile = img;
    }

    public String getImage() {
        return this.imagefile;
    }

	public String getName() {
		return name;
	}

	public List<WorldGood> getGoods() {
		return goods;
	}

	public void addGood(WorldGood g) {
		this.goods.add(g);
	}

    public int getQuantity(String goodName) {
        int q = 0;
        Iterator<WorldGood> it = this.goods.iterator();
        while (it.hasNext()) {
            WorldGood wg = it.next();
            if (wg.getName().equals(goodName))
                return wg.getQuantity();
        }
        return q;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductQuantity() {
        return this.productQuantity;
    }

    public double getRealProductQuantity() {
        int ix = (int) (this.getRealQuantity() * this.getProductQuantity() * 100.0); // scale it
        return ix / 100.0;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public boolean hasProduct() {
        if (this.productName.isEmpty() || this.productQuantity == 0)
            return false;
        else
            return true;
    }

    public double getProductRealQuantity() {
        return this.productRealQuantity;
    }

    public void setProductRealQuantity(double productRealQuantity) {
        this.productRealQuantity = productRealQuantity;
    }

    public boolean isBoost() {
    	return this.isBoost;
    }

    /**
     * Get the real quantity of that boost that can be produced each cycle
     *
     * @return double real quantity
     */
    public double getRealQuantity() {
        List<Double> maximas = new ArrayList();
        // Loop needed goods
        Iterator<WorldGood> nit = this.goods.iterator();
        while (nit.hasNext()) {
            WorldGood wg = nit.next();
            double goodRQ = wg.getRealQuantity();
            // If we lack 1 good, we can't produce that boost, no need to go further
            if (goodRQ <= 0) {
                this.realQuantity = 0;
                return 0;
            }
            // Get the ratio
            maximas.add(goodRQ);
        }
        // Sort maximas desc
        if (maximas.size() < 1)
            return 0;

        Collections.sort(maximas); // default sorting is ASCENDING
        double max = maximas.get(0); // the first value is the lowest :)

        // scale
        int ix = (int) (max * 100.0); // scale it
        max = ix / 100.0;
        // return
        this.realQuantity = max;
        return this.realQuantity;
    }

    public void setRealQuantity(double realQuantity) {
        this.realQuantity = realQuantity;
    }

    /**
     * Get the list of goods not produced by the player, and needed to make that boost
     *
     * @return List<WorldGood>
     */
    public List<WorldGood> getLackingGoods() {
        List<WorldGood> list = new ArrayList<WorldGood>();
        // Loop needed goods
        Iterator<WorldGood> nit = this.goods.iterator();
        while (nit.hasNext()) {
            WorldGood wg = nit.next();
            if (wg.getRealQuantity() <= 0)
                list.add(wg);
        }
        return list;
    }


}
