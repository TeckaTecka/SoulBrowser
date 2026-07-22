<?php
class Eshop_Model_DbTable_Manufacturers extends Zend_Db_Table_Abstract
{
	protected $_name = 'manufacturers';
	protected $_primary = 'id';
	
	/**
     * Returns array of Manufacturers
     * @return	array
     */
	public function getManufacturers($page = NULL)
	{
		$select = $this->select();
		$select
			->order('title ASC')
			->where('id <> 1');
		if($page){
			$select->limitPage($page, 8);
		}
		$result = $this->fetchAll($select);
				
		if (count($result)==0) {
			return NULL;
		} else {
			$result = $result->toArray();
			return $result;
		}
	}
	/**
     * Returns array of manufactury
     * @param	string	$title_url
     * @return	array
     */
	public function getManufacturyByTitleUrl($title_url)
	{
		$select = $this->select();
		$select->where('title_url = ?', $title_url);
		$result = $this->fetchRow($select);
				
		if (count($result)==0) {
			return NULL;
		} else {
			$result = $result->toArray();
			return $result;
		}
	}
	/**
     * Returns array of Manufactury
     * @return	array
     */
	public function getManufactury($id)
	{
		$select = $this->select();
		$select
			->where('id = ?',$id);
		$result = $this->fetchRow($select);
				
		if (count($result)==0) {
			return NULL;
		} else {
			$result = $result->toArray();
			return $result;
		}
	}
}