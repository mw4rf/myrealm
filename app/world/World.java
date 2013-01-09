package world;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import models.realm.Building;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class World {

	public static HashMap<String,WorldBuilding> BUILDINGS = new HashMap();
    public static HashMap<String,BuildingGoodMap> GOODS = new HashMap();
	public static HashMap<String,WorldAdventure> ADVENTURES = new HashMap();
	public static HashMap<String,WorldBoost> BOOSTS = new HashMap();
	public static HashMap<String,WorldAvatar> AVATARS = new HashMap();
    public static HashMap<String,WorldSoldier> SOLDIERS = new HashMap();
    public static HashMap<String,CustomStyle> CUSTOMSTYLES = new HashMap();
    public static HashMap<String,WorldGuild> GUILDS = new HashMap();
    public static HashMap<Integer,Tip> TIPS = new HashMap();

	public World() {

		try {
            loadCustomStyles();
			loadAvatars();
            loadGuilds();
            loadBuildings();
            loadGoods();
            loadAdventures();
            loadBoosts();
            loadSoldiers();
            loadTips();
		}//try
		catch (Exception e) {e.printStackTrace();}
	}

    /**
     * Load CSS Styles from XML
     * @throws Exception
     */
	private void loadCustomStyles() throws Exception {
        InputStream XMLfile = getClass().getResourceAsStream("customstyles.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XMLfile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("style");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                String css = eElement.getAttribute("css");
                String name = eElement.getAttribute("name");

                CustomStyle cs = new CustomStyle(name, css);
                CUSTOMSTYLES.put(name, cs);
                System.out.println("Custom style loaded: " + name);
            }//if
        }//for
    }

	/**
	 * Load player Avatars from XML
	 * @throws Exception
	 */
	private void loadAvatars() throws Exception {
		InputStream XMLfile = getClass().getResourceAsStream("avatars.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XMLfile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("avatar");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                String id = eElement.getAttribute("id");
                String name = eElement.getAttribute("name");

                WorldAvatar a = new WorldAvatar(id, name);
                AVATARS.put(id, a);
                System.out.println("Avatar loaded: " + name);
            }//if
        }//for
    }

	/**
	 * Load guild Banners from XML
	 * @throws Exception
	 */
    private void loadGuilds() throws Exception {
        InputStream XMLfile = getClass().getResourceAsStream("guilds.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XMLfile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("guild");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                String id = eElement.getAttribute("id");
                String name = eElement.getAttribute("name");

                WorldGuild a = new WorldGuild(id, name);
                GUILDS.put(id, a);
                System.out.println("Guild banner loaded: " + name);
            }//if
        }//for
    }

    /**
     * Load Buildings from XML
     * @throws Exception
     */
	private void loadBuildings() throws Exception {
		InputStream XMLfile = getClass().getResourceAsStream("buildings.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XMLfile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("building");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                // Building name
                String buildingName = eElement.getAttribute("name");
                // Building image
                String buildingImage = "images/buildings/" + eElement.getAttribute("imgid") + ".png";
                // Building expiration
                boolean expires = Boolean.parseBoolean(eElement.getAttribute("expires"));
                // Base production time
                int basetime = 1;
                try {
                	basetime = Integer.parseInt(eElement.getAttribute("basetime"));
                } catch(Exception e) {e.printStackTrace();}

                // Goods
                ArrayList<WorldGood> buildingGoods = new ArrayList();

                NodeList gList = eElement.getElementsByTagName("good");
                for(int gtemp = 0; gtemp < gList.getLength(); gtemp++) {
                	Node gNode = gList.item(gtemp);
                	if(gNode.getNodeType() == Node.ELEMENT_NODE) {
                		Element gElement = (Element) gNode;

                		String gName = gElement.getAttribute("name");
                		int gQuantity = Integer.parseInt(gElement.getAttribute("quantity"));
                		String goodImage = "images/goods/" + gElement.getAttribute("imgid") + ".png";

                		WorldGood wg = new WorldGood(gName, gQuantity);
                		wg.setImage(goodImage);
                		buildingGoods.add(wg);
                	}
                }//end goods
                WorldBuilding b = new WorldBuilding(buildingName, buildingGoods);
                b.setImage(buildingImage);
                b.expires = expires;
                b.basetime = basetime;
                BUILDINGS.put(buildingName, b);
                System.out.println("Building loaded: " + buildingName);
            }//if
        }//for
	}

	/**
	 * Load goods from Buildings
	 * WARNING : this method must be called AFTER loadBuildings()
	 * @throws Exception
	 */
    private void loadGoods() throws Exception {
        // Loop buildings
        Iterator<WorldBuilding> it = World.BUILDINGS.values().iterator();
        while (it.hasNext()) {
            WorldBuilding wb = it.next();
            // Loop building's goods
            Iterator<WorldGood> wit = wb.goods.iterator();
            while (wit.hasNext()) {
                WorldGood wg = wit.next();
                // Create the good if needed
                if (!World.GOODS.containsKey(wg.name)) {
                    BuildingGoodMap newbgm = new BuildingGoodMap(wg);
                    World.GOODS.put(wg.name, newbgm);
                    System.out.println("Good loaded: " + wg.name);
                }
                // Update the good
                BuildingGoodMap bgm = World.GOODS.get(wg.name);
                // Produced
                if (wg.getQuantity() > 0)
                    bgm.producedBy.put(wb.name, wb);
                // Needed
                else
                    bgm.neededBy.put(wb.name, wb);
            }
        }
    }

    /**
     * Load Adventures from XML
     * @throws Exception
     */
	private void loadAdventures() throws Exception  {
		InputStream XMLfile = getClass().getResourceAsStream("adventures.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XMLfile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("adventure");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                // Adventure name
                String adventureName = eElement.getAttribute("name");
                // Adventure image
                String adventureImage = "images/adventures/" + eElement.getAttribute("imgid") + ".png";
                // Integers
                int players = Integer.parseInt(eElement.getAttribute("players"));
                int difficulty = Integer.parseInt(eElement.getAttribute("difficulty"));
                int days = Integer.parseInt(eElement.getAttribute("days"));

                WorldAdventure a = new WorldAdventure(adventureName);
                a.setImage(adventureImage);
                a.players = players;
                a.difficulty = difficulty;
                a.days = days;
                ADVENTURES.put(adventureName, a);
                System.out.println("Adventure loaded: " + adventureName);
            }//if
        }//for
	}

	/**
	 * Load Boosts from XML
	 * @throws Exception
	 */
	private void loadBoosts() throws Exception {
		InputStream XMLfile = getClass().getResourceAsStream("boosts.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XMLfile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("boost");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                // Boost name
                String boostName = eElement.getAttribute("name");

                // Boost image
                String boostImage = "images/boosts/" + eElement.getAttribute("imgid") + ".png";

                // is boost ? (or something else)
                boolean isBoost = Boolean.parseBoolean(eElement.getAttribute("isboost"));

                // Product name & quantity
                String productName = "";
                int productQuantity = 0;
                if (!eElement.getAttribute("product_name").isEmpty() && !eElement.getAttribute("product_quantity").isEmpty()) {
                    productName = eElement.getAttribute("product_name");
                    productQuantity = Integer.parseInt(eElement.getAttribute("product_quantity"));
                }

                // Goods
                ArrayList<WorldGood> boostGoods = new ArrayList();

                NodeList gList = eElement.getElementsByTagName("good");
                for(int gtemp = 0; gtemp < gList.getLength(); gtemp++) {
                	Node gNode = gList.item(gtemp);
                	if(gNode.getNodeType() == Node.ELEMENT_NODE) {
                		Element gElement = (Element) gNode;

                		String gName = gElement.getAttribute("name");
                		int gQuantity = Integer.parseInt(gElement.getAttribute("quantity"));

                		WorldGood wg = new WorldGood(gName, gQuantity);
                		String imagefile = World.GOODS.get(gName).getGood().getImage();
                		wg.setImage(imagefile);
                		boostGoods.add(wg);
                	}
                }//end goods
                WorldBoost b = new WorldBoost(boostName, boostGoods);
                b.setProductName(productName);
                b.setProductQuantity(productQuantity);
                b.setImage(boostImage);
                b.isBoost = isBoost;
                BOOSTS.put(boostName, b);
                System.out.println("Boost loaded: " + boostName);
            }//if
        }//for
	}

	/**
	 * Load military units from XML
	 * @throws Exception
	 */
    private void loadSoldiers() throws Exception {
        InputStream XMLfile = getClass().getResourceAsStream("soldiers.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XMLfile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("soldier");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                // Soldier name
                String soldierName = eElement.getAttribute("name");
                // Soldier image
                String soldierImage = "images/soldiers/" + eElement.getAttribute("imgid") + ".png";

                // Goods needed to train him
                ArrayList<WorldGood> soldierGoods = new ArrayList();

                NodeList gList = eElement.getElementsByTagName("good");
                for (int gtemp = 0; gtemp < gList.getLength(); gtemp++) {
                    Node gNode = gList.item(gtemp);
                    if (gNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element gElement = (Element) gNode;

                        String gName = gElement.getAttribute("name");
                        int gQuantity = Integer.parseInt(gElement.getAttribute("quantity"));

                        WorldGood wg = new WorldGood(gName, gQuantity);
                        String imagefile = World.GOODS.get(gName).getGood().getImage();
                        wg.setImage(imagefile);
                        soldierGoods.add(wg);
                    }
                }//end goods
                WorldSoldier s = new WorldSoldier(soldierName, soldierGoods);
                s.setImage(soldierImage);
                SOLDIERS.put(soldierName, s);
                System.out.println("Soldier loaded: " + soldierName);
            }//if
        }//for
    }
    
    /**
     * Load Tips from XML
     * @throws Exception
     */
	private void loadTips() throws Exception {
        InputStream XMLfile = getClass().getResourceAsStream("tips.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(XMLfile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("tip");
        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                String value = eElement.getAttribute("value");
                int id = Integer.parseInt(eElement.getAttribute("id"));
                String link = null;
                if(!eElement.getAttribute("link").isEmpty())
                	link = eElement.getAttribute("link");

                Tip t = new Tip(id, value, link);
                TIPS.put(id, t);
                System.out.println("Tip loaded: " + t.toString());
            }//if
        }//for
    }

} // end class

