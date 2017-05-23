import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
//@invariant (max_occupancy != 0)
class House {
	
	private int house_num;
	private int max_occupancy;
	private boolean amenities[] = new boolean[2]; //[0] -> is_parking_available, [1] -> is_wifi_available
	static HashMap<Integer,Landlord> house_landlord_map = new HashMap<Integer,Landlord>();
	private LinkedList<String> reviews = new LinkedList<String>();
	private boolean house_full = false;
	private boolean house_empty = true;
	private Street street;
	private int rent;
	private int safety_index;
	public String reviewed_by_advisor;
	
	public House(int house_num, boolean amenities[], int max_occupancy){  // constructor for House
		this.house_num = house_num;
		this.amenities = amenities;
		this.max_occupancy = max_occupancy;
	}
	
	public void set_street(Street s)
	{
		this.street = s;
	}
	
	
	public void calculate_rent()
	{
		Rent_Calculator calc = new Rent_Calculator();
		this.rent = calc.get_calculated_rent(this.street);
	}
	
	public void calculate_safety_index()
	{
		Safety_Calculator calc = new Safety_Calculator();
		this.safety_index = calc.get_calculated_safety_index(this.street);
	}

	public void update_review(String review)
	{
		reviews.add(review);
	}
	
	public int get_house_num(){
		return house_num;
	}
	
	public boolean[] get_house_amenities() {
		return amenities;
	}

	public boolean isHouse_full() {
		return house_full;
	}

	public void setHouse_full(boolean house_full) {
		this.house_full = house_full;
	}

	public int getMax_occupancy() {
		return max_occupancy;
	}
	//@Requires(max_occupancy >=0 )
	//@Ensures(max_occupancy > 0)
	public void setMax_occupancy(int max_occupancy) {
		this.max_occupancy = max_occupancy;
	}

	public boolean isHouse_empty() {
		return house_empty;
	}

	public void setHouse_empty(boolean house_empty) {
		this.house_empty = house_empty;
	}

	public int getRent() {
		return rent;
	}

	public int getSafety_index() {
		return safety_index;
	}
}


//@invariant end_date > start_date
class Lease {
	private String lease_id_prepend = "000";
	private String lease_id;
	private String statement;
	private String start_date;
	private String end_date;
	static HashMap<String,LinkedList<Tenant>> lease_tenants_map = new HashMap<String,LinkedList<Tenant>>();
	static HashMap<String,Landlord> lease_landlord_map = new HashMap<String,Landlord>();
	public Lease(int house_num, String statement, String start_date, String end_date)
	{
		lease_id = lease_id_prepend + house_num;
		statement = this.statement;
		start_date = this.start_date;
		end_date = this.end_date;
	}
	
	public String get_lease_id()
	{
		return lease_id;
	}
	
	public String get_statement()
	{
		return statement;
	}
	
	public void update_statement(String new_statement)
	{
		statement = new_statement;
	}
	
	public void end_lease(String new_end_date)
	{
		end_date = new_end_date;
	}
}

//@invariant end_date > start_date
class Sub_Lease extends Lease
{
	private String sublease_id_prepend = "001";
	String sub_lease_id;
	static HashMap<String, String> subLease_creator_map = new HashMap<String, String>();
	static HashMap<String, String> subLease_tenant_map = new HashMap<String, String>();
	public Sub_Lease(int house_num, String statement, String start_date, String end_date) {
		super(house_num, statement, start_date, end_date);
		sub_lease_id = sublease_id_prepend + house_num;
	}
	
}

//@invariant: leases_created <= houses_owned
class Landlord {

	private String name;
	private LinkedList<House> houses_owned = new LinkedList<House>();
	private LinkedList<Lease> leases_created = new LinkedList<Lease>();
	private HashMap<Tenant, House> tenant_house_req_map = new HashMap<Tenant, House>();
	private LinkedList<String> reviews = new LinkedList<String>();
	
	//@Requires(houses_owned.size() >= 0)
	//@Ensures(houses_owned.size() = old(houses_owned.size()) + 1)
	public void register_house(House h){
	
		House.house_landlord_map.put(h.get_house_num(),this);   //mapping house to its specific landlord
		houses_owned.add(h);                     // adding registered house to the landlord
		h.calculate_rent();
		h.calculate_safety_index();
		System.out.println("Use Case 01: " +this.name+ " registered house "+h.get_house_num());
	}
	
	
	public void update_review(String review)
	{
		reviews.add(review);
	}
	
	public void create_lease(Lease lease){
		Lease.lease_landlord_map.put(lease.get_lease_id(), this);
		leases_created.add(lease);
	}
	
	public Landlord(String name){
		this.name=name;
	}
	
	public String get_name(){
		return name;
	} 
	//@Requires(tenant_house_req_map.size() >= 0)
	//@Ensures(tenant_house_req_map.size() = old(tenant_house_req_map.size()) + 1)
	public void add_house_request(Tenant t, House h) {
		tenant_house_req_map.put(t, h);
		//`System.out.println("Tenant " + t.getName() + " requested for house " + h.get_house_num());
	}
	
	public void select_tenants(String occupation_choice, char gender_choice, boolean need_vegan) {
		for(Entry<Tenant, House> e: tenant_house_req_map.entrySet())
		{
			Tenant t = e.getKey();
			House h = e.getValue();
			//if requirements meet then add the tenant to the house lease
			if ((t.getOccupation().equals(occupation_choice) && t.getGender() == gender_choice) && (need_vegan == t.is_tenant_vegan() || need_vegan == false))
			{
				String house_lease_id = "000" + h.get_house_num();
				//check if lease has already been created
				if(Lease.lease_tenants_map.containsKey(house_lease_id)) //if lease present
				{
					LinkedList<Tenant> tenant_list = Lease.lease_tenants_map.get(house_lease_id);
					tenant_list.add(t);
					if(tenant_list.size() == h.getMax_occupancy())
					{
						h.setHouse_full(true);
					}
				}
				
				else //create new lease and add tenant to it
				{
					this.create_lease(new Lease(h.get_house_num(), "Tenant " + t.getName() + "has rented house " + h.get_house_num(), "01 Dec 2016", "01 Jan 2017"));
					LinkedList<Tenant> new_tenant_list = new LinkedList<Tenant>();
					new_tenant_list.add(t);
					Lease.lease_tenants_map.put(house_lease_id, new_tenant_list);
					System.out.println("Use Case 03: Lease created for "+t.getName());
				}	
				t.add_to_Houses_selected_for(h);
				System.out.println("Use Case 02: Tenant " + t.getName() + " selected by landlord " + this.name + " for house " + h.get_house_num());
			}
			
			else
			{
				System.out.println("Use Case 02: Tenant " + t.getName() + " rejected by landlord " + this.name + " for house " + h.get_house_num());
			}
		}
		
		tenant_house_req_map.clear();
	}
	
}

//@invariant: house_selected_for == null || house_occupied==null  || (house_occupied!=null && house_selected_for.contains(house_occupied))
class Tenant {
	private String name;
	private String occupation;
	private char gender;
	private boolean is_vegan;
	House house_occupied;
	private char roommate_preferences[] = new char[2];
	private LinkedList<Lease> subLeases_created = new LinkedList<Lease>();
	private LinkedList<House> houses_selected_for = new LinkedList<House>();
	
	
	public Tenant(String name, String occupation, char gender, boolean is_vegan)
	{
		this.name = name;
		this.occupation = occupation;
		this.gender = gender;
		this.is_vegan = is_vegan;
	}
	
	
	public char[] getRoommate_preferences() {
		return roommate_preferences;
	}

	public void setRoommate_preferences(char[] cs) {
		this.roommate_preferences = cs;
	}

	public String getName() 
	{
		return name;
	}

	public String getOccupation() {
		return occupation;
	}
	
	public char getGender() {
		return gender;
	}
	
	public boolean is_tenant_vegan()
	{
		return is_vegan;
	}
	//@Requires (city_name != null)
	//@Ensures (true)
	public LinkedList<House> house_search(String street_name, String city_name, boolean[] amenity_preferences) {
		LinkedList<House> filtered_houses = new LinkedList<>();
		boolean amenities_given = true;
		if(city_name == null) //minimum criteria to be specified is city
			return null;
		else if(!City.city_street_map.containsKey(city_name)) //city name entered is invalid
		{
			return null;
		}
		else 	//search the city for houses
		{
			if(street_name == null) //city given but no street given
			{
				if(amenity_preferences[0] == false && amenity_preferences[1] == false) //no amenity check required
				{
					amenities_given = false;
				}
				LinkedList<Street> street_array = City.city_street_map.get(city_name);
				for(Street street : street_array) // for each street in the street array
				{
					String this_street_name = street.get_street_name();
					LinkedList<House> house_array = Street.street_house_map.get(this_street_name);
					if(amenities_given == true) //check for the amenities in each house
					{
						for(House house : house_array) //for each house in the house array
						{
							if(!house_has_vacancy(house)) //check for vacancy
							{
								continue;
							}
							
							if(amenity_preferences[0] == true && amenity_preferences[1] == true) //both parking and wifi needed
							{
								if(house.get_house_amenities()[0] == true && house.get_house_amenities()[1] == true)
								{
									filtered_houses.add(house);
								}
							}

							else if(amenity_preferences[0] == true && amenity_preferences[1] == false) //only parking needed
							{
								if(house.get_house_amenities()[0] == true)
								{
									filtered_houses.add(house);
								}
							}

							else if(amenity_preferences[0] == false && amenity_preferences[1] == true) //only wifi needed
							{
								if(house.get_house_amenities()[1] == true)
								{
									filtered_houses.add(house);
								}
							}
						}
					}
					
					else //add house_array to filtered_house_list
					{

						for(House house : house_array) //for each house in the house array
						{
							if(!house_has_vacancy(house)) //check for vacancy
							{
								continue;
							}
							
							filtered_houses.add(house);
						}
					}
				}
			}
			
			
			else //street is mentioned
			{
				if(!Street.street_house_map.containsKey(street_name)) //street name entered is invalid
				{
					return null;
				}
				
				else //valid street name is mentioned
				{
					LinkedList<House> house_array = Street.street_house_map.get(street_name);
					if(amenity_preferences[0] == false && amenity_preferences[1] == false) //no amenity check required so add all houses in the street to filtered house result
					{
						for(House house : house_array) //for each house in the house array
						{
							if(!house_has_vacancy(house)) //check for vacancy
							{
								continue;
							}
							
							
							filtered_houses.add(house);
						}
					}
					
					else
					{
						for(House house : house_array) //for each house in the house array
						{
							if(!house_has_vacancy(house)) //check for vacancy
							{
								continue;
							}
							
							if(amenity_preferences[0] == true && amenity_preferences[1] == true) //both parking and wifi needed
							{
								if(house.get_house_amenities()[0] == true && house.get_house_amenities()[1] == true)
								{
									filtered_houses.add(house);
								}
							}

							else if(amenity_preferences[0] == true && amenity_preferences[1] == false) //only parking needed
							{
								if(house.get_house_amenities()[0] == true)
								{
									filtered_houses.add(house);
								}
							}

							else if(amenity_preferences[0] == false && amenity_preferences[1] == true) //only wifi needed
							{
								if(house.get_house_amenities()[1] == true)
								{
									filtered_houses.add(house);
								}
							}
						}
					}
				}
			}
			System.out.println("Use Case 04: Tenant " + this.name + " selecting houses from search result:");
			for(House h : filtered_houses)
			{
				System.out.println(" " +h.get_house_num());
			}
			return filtered_houses;
		}
			
	}
	
	//@Requires (house!=null)
	//@Ensures (house.isHouse_full() == old(house.isHouse_full()))
	public boolean house_has_vacancy(House house)
	{
		if(house.isHouse_full()) //skip house if house is already full
		{
			return false;
		}
		
		return true;
	}
	
	
	//@Requires(house != null)
	//@Ensures(!house.isHouse_empty() || true)
	public boolean house_roommates_match(House house)
	{
		if(!house.isHouse_empty()) //if tenant(s) already present then check if preferences match
		{
			String current_lease_id = "000" + house.get_house_num();
			LinkedList<Tenant> tenant_list = Lease.lease_tenants_map.get(current_lease_id);
			for(Tenant t : tenant_list)
			{
				//System.out.println(t.getName());
				
				if(t.getName().equals(this.getName()))
				{
					continue;
				}
				char loop_tenant_preferences[] = t.getRoommate_preferences();
				char curr_tenant_preferences[] = this.getRoommate_preferences();
			//	System.out.println("loop tenant preferences: " + loop_tenant_preferences[0] + " " + loop_tenant_preferences[1]);
			//	System.out.println("current tenant preferences: " + curr_tenant_preferences[0] + " " + curr_tenant_preferences[1]);
				if(!((loop_tenant_preferences[0] == this.getGender() && ((loop_tenant_preferences[1] == 'V' && this.is_tenant_vegan() == true) || (loop_tenant_preferences[1] == 'N' && this.is_tenant_vegan() == false)))  && (curr_tenant_preferences[0] == t.getGender() && ((curr_tenant_preferences[1] == 'V' && t.is_tenant_vegan() == true) || (curr_tenant_preferences[1] == 'N' && t.is_tenant_vegan() == false)))))
				{
					System.out.println("Use Case 05: Tenants " + t.getName() + " and " + this.getName() + " do not match for " + house.get_house_num());
					return false;
				}
			}
		}
		System.out.println("Use Case 05: Roommates matching for tenant "+this.getName());
		return true;
	}

	public void request_house(House house) {
		Landlord house_landlord = House.house_landlord_map.get(house.get_house_num());
		house_landlord.add_house_request(this, house);
	}
	
	//@Requires(houses_selected_for.size() >= 0)
	//@Ensures(houses_selected_for.size() = houses_selected_for.size() + 1)
	public void add_to_Houses_selected_for(House h) {
		this.houses_selected_for.add(h);
	}
	
	public LinkedList<House> get_Houses_selected_for()
	{
		return this.houses_selected_for;
	}
	
	//@Requires(house_full() = false)
	//@Ensures(house_empty() = false)
	public void occupy_house(House house) {
		this.house_occupied = house;
		if(house.isHouse_empty())
		{
			house.setHouse_empty(false);
		}
	}
	
	public House get_house_occupied()
	{
		return house_occupied;
	}

	public LinkedList<Lease> getSubLeases_created() {
		return subLeases_created;
	}
	
	//@Requires(subLeases_created.size() >= 0)
	//@Ensures (subLeases_created.size() = old(subLeases_created.size()))
	public void create_sub_lease(Sub_Lease sub_lease, Tenant t) {
		this.subLeases_created.add(sub_lease);
		Sub_Lease.subLease_creator_map.put(sub_lease.get_lease_id(), this.getName());
		Sub_Lease.subLease_tenant_map.put(sub_lease.get_lease_id(), this.getName());
	}


	public void request_review(LegalAdvisor la1, String lease_id, House h) {
		// TODO Auto-generated method stub
		la1.requestor = this;
		la1.requested_house = h;
		la1.requested_lease_id = lease_id;
		System.out.println("Use case 08: "+this.name+" requests lease review from legal advisor");
	};
}



//invariant: street_array != null
class City {
	private String city_name;
	static HashMap<String, LinkedList<Street>> city_street_map = new HashMap<String, LinkedList<Street>>();
	
	public String getCity_name() {
		return city_name;
	}
	
	public City(String name)
	{
		city_name = name;
	}
	
	public void add_street(Street s)
	{
		if(city_street_map.containsKey(this.city_name)) 
		{
			LinkedList<Street> street_array = city_street_map.get(this.city_name);
			street_array.add(s);
			city_street_map.put(this.city_name, street_array);
		}
		
		else //first entry
		{
			LinkedList<Street> street_array = new LinkedList<Street>();
			street_array.add(s);
			city_street_map.put(this.city_name, street_array);
		}
	}
}

//invariant: house_array != null
class Street {
	private String street_name;
	static HashMap<String, LinkedList<House>> street_house_map = new HashMap<String, LinkedList<House>>();
	private LinkedList<String> reviews = new LinkedList<String>();
	
	public Street(String name)
	{
		street_name = name;
	}
	//@Requires(review.size() >= 0)
	//@Ensures(review.size() = old(review.size()) + 1)
	public void update_review(String review)
	{
		reviews.add(review);
	}
	
	//@Requires(street_house_map.size() >= 0)
	//@Ensures(street_house_map.size()  = street_house_map.size() + 1)
	public void add_house(House h)
	{
		if(street_house_map.containsKey(this.street_name)) 
		{
			LinkedList<House> house_array = street_house_map.get(this.street_name);
			house_array.add(h);
			street_house_map.put(this.street_name, house_array);
			
		}
		
		else //first entry
		{
			LinkedList<House> house_array = new LinkedList<House>();
			house_array.add(h);
			street_house_map.put(this.street_name, house_array);
		}
		h.set_street(this);
		
	}
	
	public String get_street_name()
	{
		return street_name;
	}
}

//invariant: name != null
class LegalAdvisor
{
	public String requested_lease_id;
	public House requested_house;
	public Tenant requestor;
	private String name;
	public LegalAdvisor(String name)
	{
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public boolean is_legal(House h)
	{
		if(!Lease.lease_landlord_map.containsKey("000" + h.get_house_num()))
		{
			System.out.println("Use Case 10: "+this.name + " claims that the lease for house " + h.get_house_num() + " is illegal!");
			return false;
		}
		
		System.out.println("Use Case 10: "+this.name + " certifies that the lease for house " + h.get_house_num() + " is legal!");
		return true;
	}
	public void visit(House h) {
		h.reviewed_by_advisor = this.name;
		System.out.println("Use Case 09: "+h.get_house_num()+" visited by "+this.name);
		
	}
}

//invariant: name != null
class Reviewer {
	private String name;
	private HashMap<Integer, String> houses_reviewed = new HashMap<Integer, String>();
	private HashMap<String, String> landlords_reviewed = new HashMap<String, String>();
	private HashMap<String, String> streets_reviewed = new HashMap<String, String>();
	
	
	public Reviewer(String name)
	{
		this.name = name;
	}
	
	
	public String getReviewer_name() {
		return name;
	}
	
	public HashMap<Integer, String> getHouses_reviewed() {
		return houses_reviewed;
	}

	public HashMap<String, String> getLandlords_reviewed() {
		return landlords_reviewed;
	}

	public HashMap<String, String> getStreets_reviewed() {
		return streets_reviewed;
	}

	public void addHouses_reviewed(House house, String review) {
		this.houses_reviewed.put(house.get_house_num(), review);
		house.update_review(review);
		
	}

	public void addStreets_reviewed(Street street, String review) {
		// TODO Auto-generated method stub
		this.streets_reviewed.put(street.get_street_name(), review);
		street.update_review(review);
	}



	public void addLandlords_reviewed(Landlord landlord, String review) {
		// TODO Auto-generated method stub
		this.landlords_reviewed.put(landlord.get_name(), review);
		landlord.update_review(review);
	}
	
}

//invariant: base_value >= 50
class Rent_Calculator
{
	int base_value = 50;
	
	//@Requires(base_value > 0)
	//@Ensures(base_value >= old(base_value))
	public int get_calculated_rent(Street street)
	{
		if(street.get_street_name().equals("heath"))
		{
			return base_value * 6;
		}
		
		else if(street.get_street_name().equals("merrimac"))
		{
			return base_value * 8;
		}
		
		else
			return 0;
	}
}


//base_value >= 0
class Safety_Calculator
{
	int base_value = 10;
	//@Requires(base_value > 0)
	//@Ensures(base_value <= old(base_value))
	public int get_calculated_safety_index(Street street)
	{
		if(street.get_street_name().equals("heath"))
		{
			return base_value - 3;
		}
		
		else if(street.get_street_name().equals("merrimac"))
		{
			return base_value - 4;
		}
		
		else
			return 0;
	}
}

//invariant: true
class Support
{
	private String mobile_num;
	private String email;
	
	public Support ( String mobile_num, String email)
	{
		this.setMobile_num(mobile_num);
		this.setEmail(email);
	}

	public String getMobile_num() {
		return mobile_num;
	}

	public void setMobile_num(String mobile_num) {
		this.mobile_num = mobile_num;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String return_contact(String choice)
	{
		if(choice.equals("mobile"))
			return mobile_num;
		else if(choice.equals("email"))
			return email;
		else
			return null;
	}
}



public class Main {

		public static void main(String[] args){
			
		Landlord l1 = new Landlord("Neeharika");
		Landlord l2 = new Landlord("Akshadha");	
		
		City city1 = new City("buffalo");
		
		Street street1 = new Street("heath");
		Street street2 = new Street("merrimac");
		
		House house1 = new House(100, new boolean[] {true, false},1);
		House house2 = new House(101, new boolean[] {true, true},2);
		House house3 = new House(200, new boolean[] {false, true},1);
		House house4 = new House(201, new boolean[] {false, false},3);
		
		city1.add_street(street1);
		city1.add_street(street2);
		
		street1.add_house(house1);
		street1.add_house(house2);
		street2.add_house(house3);
		street2.add_house(house4);
		
		
		l1.register_house(house1);
		System.out.println("Use Case 14: Rent for house " + house1.get_house_num() + " is " + house1.getRent() + " and safety_index is " + house1.getSafety_index());
		l1.register_house(house2);
		System.out.println("Use Case 15: Rent for house " + house2.get_house_num() + " is " + house2.getRent() + " and safety_index is " + house2.getSafety_index());
		l2.register_house(house3);
		System.out.println("Rent for house " + house3.get_house_num() + " is " + house3.getRent() + " and safety_index is " + house3.getSafety_index());
		l2.register_house(house4);
		System.out.println("Rent for house " + house4.get_house_num() + " is " + house4.getRent() + " and safety_index is " + house4.getSafety_index());

//LANDLORDS HAVE REGISTERED HOUSES
		
		Tenant t1 = new Tenant("Muthu", "student", 'M', true);
		Tenant t2 = new Tenant("Himal", "student", 'M', true);
		
		t1.setRoommate_preferences(new char[] {'M','V'});
		t2.setRoommate_preferences(new char[] {'M','V'});

		
		// Legal Advisor
		LegalAdvisor la1 = new LegalAdvisor("Trump");
		LegalAdvisor la2 = new LegalAdvisor("Hillary");
		
		//test1: get prospective houses for t1 and make selection
		LinkedList<House> my_house_array = t1.house_search(null, "buffalo", new boolean[] {false, false});
		
		//assume tenant chooses a random house from the array of houses
		int random_index = new Random().nextInt(my_house_array.size());
		t1.request_house(my_house_array.get(random_index));
		if(my_house_array.size() > random_index + 1)
		{
			t1.request_house(my_house_array.get(random_index + 1));
		}
		
		
		//test2: get prospective houses for t2 and make selection
		LinkedList<House> my_house_array1 = t2.house_search("heath", "buffalo", new boolean[] {false, true});
		
		random_index = new Random().nextInt(my_house_array1.size());
		t2.request_house(my_house_array1.get(random_index));
		if(my_house_array1.size() > random_index + 1)
		{
			t2.request_house(my_house_array1.get(random_index + 1));
		}
		
		
		//landlords select tenants
		l1.select_tenants("student", 'M', true);
		l2.select_tenants("student", 'F', false);
		
		//get the list of house options available for tenant and occupy a house
		LinkedList<House> houses_selected_for_t1 = t1.get_Houses_selected_for();
		//System.out.print("Tenant " + t1.getName() + " selected for house(s) ");
		/*for(House h : houses_selected_for_t1)
		{
			System.out.print(h.get_house_num() + " ");
		}
		
		System.out.println("\n");
		*/
		LinkedList<House> houses_selected_for_t2 = t2.get_Houses_selected_for();
		/*System.out.print("Tenant " + t2.getName() + " selected for house(s) ");
		for(House h : houses_selected_for_t2)
		{
			System.out.print(h.get_house_num() + " ");
		}
		
		System.out.println("\n");
		*/
		//assume tenant occupies the first acknowledged request for which roommates match 
		if(houses_selected_for_t1.size() > 0)
		{
			for(House h : houses_selected_for_t1)
			{
				if(!t1.house_roommates_match(h))
				{
					continue;
				}
				t1.request_review(la1, "000" + h.get_house_num(), h);
				la1.visit(h);
				if(!la1.is_legal(h))
				{
					continue;
				}
				t1.occupy_house(h);
				System.out.println("Use Case 06: Tenant " + t1.getName() + " occupied house " + h.get_house_num() + " owned by " + House.house_landlord_map.get(h.get_house_num()).get_name());
				break;
				
			}
			
		}
		
		else
			//System.out.println("All landlords have rejected tenant " + t1.getName());
		
		//assume tenant occupies the first acknowledged request for which roommates match 
		if(houses_selected_for_t2.size() > 0)
		{
			for(House h : houses_selected_for_t2)
			{
				if(!t2.house_roommates_match(h))
				{
					continue;
				}
				t2.request_review(la2, "000" + h.get_house_num() ,h);
				la2.visit(h);
				if(!la2.is_legal(h))
				{
					continue;
				}
				t1.occupy_house(h);
				//System.out.println("Tenant " + t2.getName() + " occupied house " + h.get_house_num() + " owned by " + House.house_landlord_map.get(h.get_house_num()).get_name());
				break;
			}
			
		}
		
		else
			System.out.println("All landlords have rejected tenant " + t2.getName());
		
		
		//sublease a house
		Tenant t3 = new Tenant("Anand", "student", 'M', true);
		if(t1.get_house_occupied() != null)
		{	System.out.println("testing ---" + t1.get_house_occupied().get_house_num());
			t1.create_sub_lease(new Sub_Lease(t1.get_house_occupied().get_house_num(), "new sub lease statement", "", ""), t3);
			LinkedList<Tenant> temp_tenant_list = Lease.lease_tenants_map.get("000" + t1.get_house_occupied().get_house_num());
	
			System.out.println("Use Case 07:tenant list before sublease");
			
			for(Tenant t : temp_tenant_list)
			{
				System.out.println(t.getName());
			}
			
			
		//	temp_tenant_list.remove(t1);
			temp_tenant_list.add(t3);
			
			System.out.println("Use Case 07: updated tenant list after sublease");
			
			for(Tenant t : temp_tenant_list)
			{
				System.out.println(t.getName());
			}
		}	
			
		
		//review a house
		Reviewer reviewer = new Reviewer("Prashanth");
		
		LinkedList<House> houses_for_review = Street.street_house_map.get("heath");
		random_index = new Random().nextInt(houses_for_review.size());
		House house_reviewed = houses_for_review.get(random_index);
		reviewer.addHouses_reviewed(house_reviewed , house_reviewed.get_house_num() + " is a beautiful house!");
		
		//review a street
		LinkedList<Street> streets_for_review = City.city_street_map.get("buffalo");
		random_index = new Random().nextInt(streets_for_review.size());
		Street street_reviewed = streets_for_review.get(random_index);
		reviewer.addStreets_reviewed(street_reviewed , street_reviewed.get_street_name() + " is a very happening street!");		
		
		//review a Landlord
		Landlord landlord_reviewed = House.house_landlord_map.get(house_reviewed.get_house_num());
		reviewer.addLandlords_reviewed(landlord_reviewed , landlord_reviewed.get_name() + " is a scary lady!");
		
		
		//print reviews
		System.out.println("Reviewer: " + reviewer.getReviewer_name());
		System.out.println("Houses Reviewed:");
		for(Entry<Integer, String> e: reviewer.getHouses_reviewed().entrySet() )
		{
			System.out.println("Use Case 11: Review for house " + e.getKey() + ": " + e.getValue() );
		}
		
		
		System.out.println("Streets Reviewed:");
		for(Entry<String, String> e: reviewer.getStreets_reviewed().entrySet() )
		{
			System.out.println("Use Case 12: Review for Street " + e.getKey() + ": " + e.getValue());
		}
		
		System.out.println("Landlords Reviewed:");
		for(Entry<String, String> e: reviewer.getLandlords_reviewed().entrySet() )
		{
			System.out.println("Use Case 13: Review for Landlord " + e.getKey() + ": " + e.getValue());
		}
		
		//get support
		Support support_site1 = new Support("716 335 4475", "himaldwa@buffalo.edu");
		System.out.println("Use Case 16: "+support_site1.return_contact("mobile"));
			
	}
}
