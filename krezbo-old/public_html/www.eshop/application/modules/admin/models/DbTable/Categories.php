<?php
class Admin_Model_DbTable_Categories extends Zend_Db_Table_Abstract
{
	protected $_name = 'categories';
	protected $_primary = 'id';
	/**
     * Returns primary keys
     * @param	string	$title
     * @param	string	$title_menu = ''
     * @param	string	$title_url = ''
     * @param	string	$description = ''
     * @return	int
     */
	public function setCategory($title, $title_menu='', $title_url='', $description='')
	{
		$data = array('title'		=>	$title,
					  'title_menu'	=>	($title_menu=='')?$title:$title_menu,
    				  'title_url'	=>	($title_url=='')?$this->Convert($title):$this->Convert($title_url),
					  'description'	=>	$description);
    	return $this->insert($data);    	
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
	}*/
	/**
     * Returns array of category
     * @param	int	$id
     * @return	array
     */
	public function getCategory($id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select//->join('categories_tree',		  
		       //		  'categories_tree.categories_id=categories.id', array('sub', 'order'))
			   ->where('categories.id = ?', $id);
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Return array of all categories
     * @return	array
     */
	public function getCategoriesAll()
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
     * Update category
     * @param	int	$id
     * @param	string	$title
     * @param	string	$title_menu = ''
     * @param	string	$title_url = ''
     * @param	string	$description = ''
     * @return	void
     */
	public function updateCategory($id, $title, $title_menu='', $title_url='', $description='')
	{
		$data = array('title'		=>	$title,
					  'title_menu'	=>	($title_menu=='')?$title:$title_menu,
    				  'title_url'	=>	($title_url=='')?$this->Convert($title):$this->Convert($title_url),
					  'description'	=>	$description);
		
		$this->update($data, 'id = '.$id);
	}
	/**
     * Delete category
     * @param	int	$id
     * @return	void
     */
	public function delCategory($id)
    {
    	$product2CategoriesTab = new Admin_Model_DbTable_Products2Categories();
    	$product2CategoriesTab->delProductByCategoryID($id);
    	$this->delete('id = '.$id);
    }
    /**
     * Return true/false if url ofcategory exist
     * @param	string	$category_url
     * @return	boolean
     */
	public function existUrlCategory($category_url)
	{
		$select = $this->select()->where('title_url = ?', $category_url);
		$rows = $this->fetchAll($select);
		if($rows)
		{
			return count($rows);
		}
		return false;
	}
	/**
     * Return converted text
     * @param	string	$text
     * @return	string
     */
	public function Convert($text)
	{
		$ar = array(' '=> '-', '&'=>'-', ':'=>'-', '.'=>'-', ','=>'-', '%'=>'-', '('=>'-', ')'=>'-', '+'=>'-', '/'=>'-',
					'á'=> 'a', 'č'=> 'c', 'ď'=> 'd', 'é'=> 'e', 'ě'=> 'e', 'í'=> 'i', 'ň'=> 'n', 'ó'=> 'o',
					'ř'=> 'r', 'š'=> 's', 'ť'=> 't', 'ú'=> 'u', 'ů'=> 'u', 'ý'=> 'y', 'ž'=> 'z',
					'Á'=> 'A', 'Č'=> 'C', 'Ď'=> 'D', 'É'=> 'E', 'Ě'=> 'E', 'Í'=> 'I', 'Ň'=> 'N', 'Ó'=> 'O',
					'Ř'=> 'R', 'Š'=> 'S', 'Ť'=> 'T', 'Ú'=> 'U', 'Ů'=> 'U', 'Ý'=> 'Y', 'Ž'=> 'Z' );
		
		foreach ($ar as $key=>$value) {
			$text = str_replace($key, $value, $text);
		}		
		$text = strtolower($text);
		return $text;
	}
	
}