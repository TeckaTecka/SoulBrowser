<?php
class Eshop_Model_DbTable_Pictures extends Zend_Db_Table_Abstract
{
	protected $_name = 'pictures';
	protected $_primary = 'id';
	
	/**
     * Return array of pictures
     * @param int	$product_id
     * @return	array
     */
	public function getPictures($product_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('products2pictures',		  
		       		  'products2pictures.pictures_id = pictures.id', array())
			   ->where('products2pictures.products_id = ?', $product_id)
			   ->order('pictures.active DESC')
			   ->order('pictures.order ASC');
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Return array of picture
     * @param int	$product_id
     * @return	array
     */
	public function getPicture($product_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('products2pictures',		  
		       		  'products2pictures.pictures_id = pictures.id', array())
			   ->where('products2pictures.products_id = ?', $product_id)
			   ->where('active = 1')
			   ->order('pictures.order ASC');
		$row = $this->fetchRow($select);
		
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
}