<?php
class Admin_Model_DbTable_Addresses extends Zend_Db_Table_Abstract
{
	protected $_name = 'addresses';
	protected $_primary = 'id';
	/**
     * Return array of billing Address by order
     * @param int	$order_id
     * @return	array
     */
	public function getBillingAddress($order_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('orders2addresses',		  
		       		  'orders2addresses.addresses_id = addresses.id', array('billing', 'delivery'))
			   ->join('countries',
			   		  'countries.id = addresses.countries_id', array('country'))
			   ->where('orders2addresses.orders_id = ?', $order_id)
			   ->where('orders2addresses.billing = ?', true);
		$row = $this->fetchRow($select);
		
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Return array of delivery Address by order
     * @param int	$order_id
     * @return	array
     */
	public function getDeliveryAddress($order_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('orders2addresses',		  
		       		  'orders2addresses.addresses_id = addresses.id', array('billing', 'delivery'))
			   ->join('countries',
			   		  'countries.id = addresses.countries_id', array('country'))
			   ->where('orders2addresses.orders_id = ?', $order_id)
			   ->where('orders2addresses.delivery = ?', true);
		$row = $this->fetchRow($select);
		
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Update Address to DB
     * @param	int		$id
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
	public function updateAddress($id, $street, $street_nr, $city, $zip, $countries_id,
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
    	
        $this->update($data, 'id = '.$id);
    }
}