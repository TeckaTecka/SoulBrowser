<?php
class Cart_Model_DbTable_Categories extends Zend_Db_Table_Abstract
{
	protected $_name = 'categories';
	protected $_primary = 'id';
	
	/*/**
     * Returns array of subcategories
     * @param	int	$sub = 1
     * @return	array
     */
/*	public function getCategories($sub = 0)
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
     * Return array of all categories
     * @param	int	$sub = 0
     * @return	array
     */
/*	public function getCategoriesAll($sub = 0)
	{
		$search = $this->getCategories($sub);
		$cat = $search;
		
		for ($i = 0; $i < count($cat); $i++) {
			$search = $this->getCategories($cat[$i]['id']);
			if (count($search)<>0){
				$pom = $cat;
				for ($y = 0; $y < count($search); $y++) {
					$cat[$i+$y+1] = $search[$y];
				}
				for ($z = $i+1; $z < count($pom); $z++) {
					$cat[$y+$z] = $pom[$z];
				}
			}
		}
		return $cat;
	}
	/**
     * Returns array of category
     * @param	string	$title_url
     * @return	array
     */
/*	public function getCategoryByTitleUrl($title_url)
	{
		$select = $this->select();
		$select->where('categories.title_url = ?', $title_url);
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Returns array of category
     * @param	int	$id
     * @return	array
     */
	public function getCategory($id)
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
	}
}