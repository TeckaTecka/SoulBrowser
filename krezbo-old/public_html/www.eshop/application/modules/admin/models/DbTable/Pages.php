<?php
class Admin_Model_DbTable_Pages extends Zend_Db_Table_Abstract
{
	protected $_name = 'pages';
	protected $_primary = 'id';
	/**
     * Returns primary keys
     * @param	string	$title
     * @param	string	$title_menu = ''
     * @param	string	$title_url = ''
     * @param	string	$page = ''
     * @return	int
     */
	public function setPage($title, $title_menu='', $title_url='', $page='', $show)
	{
		$data = array('title'		=>	$title,
					  'title_menu'	=>	($title_menu=='')?$title:$title_menu,
    				  'title_url'	=>	($title_url=='')?$this->Convert($title):$this->Convert($title_url),
					  'page'		=>	$page,
		'show'		=>	$show);
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
		$select->join('categories_bd',		  
		       		  'categories_bd.categories_id=categories.id', array('sub', 'order'))
			   ->order(array('categories_bd.order ASC'))
		       ->where('categories_bd.sub = ?', $sub);
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
     * Return array of all pages
     * @param	int	$outside = 1
     * @return	array
     */
	public function getPagesAll($outside = 1)
	{
		$select = $this->select();
		$select->where('"show" IS NOT NULL')
		       ->where('id <> ?', $outside);
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			return $rows;
		}
	}
	/**
     * Update page
     * @param	int	$id
     * @param	string	$title
     * @param	string	$title_menu = ''
     * @param	string	$title_url = ''
     * @param	string	$page = ''
     * @return	void
     */
	public function updatePage($id, $title, $title_menu='', $title_url='', $page='', $show)
	{
		$data = array(
			'title'			=>	$title,
			'title_menu'	=>	($title_menu=='')?$title:$title_menu,
    		'title_url'		=>	($title_url=='')?$this->Convert($title):$this->Convert($title_url),
			'page'			=>	$page,
			'show'			=>	$show);
		
		$this->update($data, 'id = '.$id);
	}
	/**
     * Delete page
     * @param	int	$id
     * @return	void
     */
	public function delPage($id)
    {
    	$this->delete('id = '.$id);
    }
	/**
     * Return converted text
     * @param	string	$text
     * @return	string
     */
	private function Convert($text)
	{
		$ar = array(' '=> '-', '&'=>'-', ':'=>'-', '.'=>'-', ','=>'-', '%'=>'-', '('=>'-', ')'=>'-',
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
	/*
	
	public function getMenuOfParents($sub)
	{
		$data = array();
		$i = 0;
		do{
			$select = $this->select()
	    				   ->where('id = ?', $sub);
			$row = $this->fetchRow($select);
			if ($row){
				$row = $row->toArray();
				$sub = $row['sub'];
				$data[$i]['title'] = $row['title_menu'];
				$data[$i]['id'] = $row['id'];
				$i++;
			}
		}while ($row);
		return $data;
	}
	
	
	public function getNextOrder($sub)
	{
		$select = $this->select()
					   ->from('categories', 'COUNT(*)')
					   ->where('sub = ?', $sub);
		$row = $this->fetchRow($select)->toArray();
		return (int)$row['COUNT(*)'];
	}
	public function getCategory($id)
	{
		$select = $this->select()
	    			   ->where('id = ?', $id);
		$row = $this->fetchRow($select);
		if ($row){
			return $row->toArray();
		}else{
			return NULL;
		}
	}
	
	
	
	}*/
}