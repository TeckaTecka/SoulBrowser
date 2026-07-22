<?php
class Admin_Model_DbTable_Countries extends Zend_Db_Table_Abstract
{
	protected $_name = 'countries';
	protected $_primary = 'id';
	/**
     * Return array of countries
     * @return	array
     */
	public function getCountries()
	{
		$select = $this->select();
		$rows = $this->fetchAll($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Return pairs of countries
     * @return	array
     */
	public function getCountriesPairs()
	{
		$select = $this->select();
		$rows = $this->_db->fetchPairs($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			return $rows;
		}
	}
	/**
     * Set Country
     * @param	string	$country
     * @param	boolean	$show
     * @return	int
     */
	public function setCountry($country, $show)
	{
		$data = array('country'	=>	$country,
					  'show'	=>	$show);
    	return $this->insert($data);    	
	}
	/**
     * Return array of country
     * @return	array
     */
	public function getCountry($id)
	{
		$select = $this->select();
		$select->where('id = ?', $id);
		$row = $this->fetchRow($select);
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Update country
     * @param	int		$id
     * @param	string	$country
     * @param	int		$show
     * @return	void
     */
	public function updateCountry($id, $country, $show)
	{
		$data = array(
			'country'	=>	$country,
			'show'		=>	$show
		);
		
		$this->update($data, 'id = '.$id);
	}
	/**
     * Delete country
     * @param	int	$id
     * @return	void
     */
	public function delCountry($id)
    {
    	$this->delete('id = '.$id);
    }
}