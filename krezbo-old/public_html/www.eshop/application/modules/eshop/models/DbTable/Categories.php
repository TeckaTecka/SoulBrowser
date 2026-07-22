<?php
class Eshop_Model_DbTable_Categories extends Zend_Db_Table_Abstract
{
	protected $_name = 'categories';
	protected $_primary = 'id';
	
	/**
     * Return array of all categories
     * @return array
     */
	public function getCategories()
	{
		$select = $this->select();
		$result = $this->fetchAll($select);
				
		if (count($result)==0) {
			return NULL;
		} else {
			$result = $result->toArray();
			return $result;
		}
	}
	/**
     * Returns array of category
     * @param	string	$title_url
     * @return	array
     */
	public function getCategoryByTitleUrl($title_url)
	{
		$select = $this->select();
		$select->where('categories.title_url = ?', $title_url);
		$result = $this->fetchRow($select);
				
		if (count($result)==0) {
			return NULL;
		} else {
			$result = $result->toArray();
			return $result;
		}
	}
	/**
     * Returns array of category (only one category)
     * @param	int	$product_id
     * @return	array
     */
	public function getCategoryByProductId($product_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('products2categories',		  
		       		  'products2categories.categories_id = categories.id', array())
			   ->where('products2categories.products_id = ?', $product_id);
		$result = $this->fetchRow($select);
				
		if (count($result)==0) {
			return NULL;
		} else {
			$result = $result->toArray();
			return $result;
		}
	}
	/**
     * Returns array of subcategories
     * @param	int	$sub = 1
     * @return	array
     */
	/*public function getCategories($sub = 0)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('categories_tree',		  
		       		  'categories_tree.categories_id=categories.id', array('sub', 'order'))
			   ->order(array('categories_tree.order ASC'))
		       ->where('categories_tree.sub = ?', $sub);
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	
	
	/**
     * Returns array of category
     * @param	int	$id
     * @return	array
     */
	/*public function getCategory($id)
	{
		$select = $this->select();
		$select->where('categories.id = ?', $id);
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}*/
}