<?php
class Eshop_Model_DbTable_Pages extends Zend_Db_Table_Abstract
{
	protected $_name = 'pages';
	protected $_primary = 'id';
	/**
     * Return array of all pages
     * @param	int	$outside = 1
     * @return	array
     */
	public function getPagesAll($outside = 1)
	{
		$select = $this->select();
		$select
			->where("`id` != ?", $outside)
			->where("`show` = ?", 1);
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}	
	/**
     * Returns array of page
     * @param	int	$id
     * @return	array
     */
	public function getPage($id)
	{
		$select = $this->select();
		$select
			->where("`id` = ?", $id)
			->where("`show` = ?", 1);
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Returns array of page
     * @param	string	$title_url
     * @return	array
     */
	public function getPageByTitleUrl($title_url)
	{
		$select = $this->select();
		$select->where('title_url = ?', $title_url);
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
}