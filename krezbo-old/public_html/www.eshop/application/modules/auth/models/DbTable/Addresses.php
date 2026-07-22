<?php
class Auth_Model_DbTable_Addresses extends Zend_Db_Table_Abstract
{
	protected $_name = 'addresses';
	protected $_primary = 'id';
	
	/**
     * Return Addresses by $user_id
     * @param	int		$user_id
     * @return	array
     */
	public function getAddressesByUserId($user_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('users2addresses',
			   		  'users2addresses.addresses_id = addresses.id',
			   		  '')
		       ->join('countries',
			   		  'countries.id = addresses.countries_id',
			   		  'country')
			   ->where('users2addresses.users_id = ?', $user_id)
			   ->where('addresses.flags IS NULL')
		 	   ->order('addresses.id DESC');
  
		$rows = $this->fetchAll($select);
				
		if (count($rows)<>0) {
			return $rows->toArray();
		} else {
			return NULL;
		}
	}
	/**
     * Add Address to DB
     * @param	string	$street
     * @param	int		$street_nr
     * @param	string	$city
     * @param	int		$zip
     * @param	int		$countries_id
     * @param	string	$contact_person
     * @param	string	$contact_email
     * @param	string	$contact_phone
     * @param	string	$person_title
     * @param	string	$person_name
     * @param	string	$person_surname
     * @param	string	$company_name
     * @param	int		$company_identification
     * @param	int		$company_vat
     * @return	primary key
     */
	public function setAddress($street, $street_nr, $city, $zip, $countries_id,
							   $contact_person, $contact_email, $contact_phone,
							   $person_title, $person_name, $person_surname,
							   $company_name, $company_identification, $company_vat)
    {
    	$data = array(
    		'street'					=>	$street,
    		'street_nr'					=>	$street_nr,
    		'city'						=>	$city,
    		'zip'						=>	$zip,
    		'countries_id'				=>	$countries_id,
    		'contact_person'			=>	($contact_person)?$contact_person:NULL,
    		'contact_email'				=>	$contact_email,
    		'contact_phone'				=>	$contact_phone,
    		'person_title'				=>	($person_title)?$person_title:NULL,
    		'person_name'				=>	($person_name)?$person_name:NULL,
    		'person_surname'			=>	($person_surname)?$person_surname:NULL,
    		'company_name'				=>	($company_name)?$company_name:NULL,
    		'company_identification'	=>	($company_identification)?$company_identification:NULL,
    		'company_vat'				=>	($company_vat)?$company_vat:NULL
    	);
        return $this->insert($data);
    }
	/**
     * set tag
	 * @param	int		$address_id
     * @param	string	$tag
     * @return	void
     */
	public function setFlag($address_id, $flag)
    {
    	$data = array('flags'	=>	$flag);
    	$this->update($data, 'id = '.$address_id);
    }
}