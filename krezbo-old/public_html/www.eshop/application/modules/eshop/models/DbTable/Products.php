<?php
class Eshop_Model_DbTable_Products extends Zend_Db_Table_Abstract
{
	protected $_name = 'products';
	protected $_primary = 'id';
	
	/**
     * Return array of search products
     * @return	array
     */
	public function searchProducts($text)
	{
		$select = $this
			->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
			->setIntegrityCheck(false);
		$select
			->join(
				'availability',		  
		       	'availability.id = products.availability_id',
				array(
					'title AS availability_title'
				)
			)			
			->join(
				'manufacturers',		  
		    	'manufacturers.id = products.manufacturers_id',
				array(
					'manufacturers.title AS manufacturers',
					'manufacturers.title_url AS manufacturers_url'
				)
			)
			->order('products.title ASC')
			->where('products.flags IS NULL')
			->where('LOWER(products.title) LIKE LOWER(?)', '%'.$text.'%');
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$currency = $this->getCurrency();
			$picturesTab = new Eshop_Model_DbTable_Pictures();
			$categoriesTab = new Eshop_Model_DbTable_Categories();
			for ($i=0;$i<count($rows);$i++) {
				$rows[$i]['picture'] = $picturesTab->getPicture($rows[$i]['id']);
				$rows[$i]['price'] = number_format($rows[$i]['price'], 0, '.', ' ').' '.$currency;
				$rows[$i]['price_orig'] = number_format($rows[$i]['price_orig'], 0, '.', ' ').' '.$currency;
				$rows[$i]['category'] = $categoriesTab->getCategoryByProductId($rows[$i]['id']);
			}
			return $rows;
		}
	}
	/**
     * Returns count of products
     * @return	int
     */
	public function getCount()
	{
		$select = $this->select()
					   ->from('products', 'COUNT(*)')
					   ->where('flags IS NULL');
		$result = $this->fetchRow($select)->toArray();
		return (int)$result['COUNT(*)'];
	}
	/**
     * Returns count of recommend products
     * @return	int
     */
	public function getRecommendCount()
	{
		$select = $this->select()
					   ->from('products', 'COUNT(*)')
					   ->where('recommend = ?', 1)
					   ->where('flags IS NULL');
		$result = $this->fetchRow($select)->toArray();
		return (int)$result['COUNT(*)'];
	}
	/**
     * Returns recommend products
     * @param int $count
     * @param int $page
     * @return	array
     */
	public function getRecommendProducts($count = NULL, $page = NULL)
	{
		$select = $this
			->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
			->setIntegrityCheck(false)
	    	->where('recommend = ?', 1)
	    	->order('title ASC')
	    	->join('availability',		  
		    	'availability.id = products.availability_id', array('title AS availability'))
			->join('manufacturers',		  
		    	'manufacturers.id = products.manufacturers_id', array('title AS manufacturers', 'title_url AS manufacturers_url'))
			   ;
	    if ($count){
	    	if($page){
	    		$select->limitPage($page, $count);
	    	}else{
	    		$select->limit($count);
	    	}
	    }
		$rows = $this->fetchAll($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$currency = $this->getCurrency();
			$picturesTab = new Eshop_Model_DbTable_Pictures();
			$categoriesTab = new Eshop_Model_DbTable_Categories();
			for ($i=0;$i<count($rows);$i++) {
				$rows[$i]['picture'] = $picturesTab->getPicture($rows[$i]['id']);
				$rows[$i]['price'] = number_format($rows[$i]['price'], 0, '.', ' ').' '.$currency;
				$rows[$i]['price_orig'] = number_format($rows[$i]['price_orig'], 0, '.', ' ').' '.$currency;
				$rows[$i]['category'] = $categoriesTab->getCategoryByProductId($rows[$i]['id']);
			}
			return $rows;
		}
	}
	/**
     * Returns count of new products
     * @return	int
     */
	public function getNewCount()
	{
		$select = $this->select()
					   ->from('products', 'COUNT(*)')
					   ->where('news = ?', 1)
					   ->where('flags IS NULL');
		$result = $this->fetchRow($select)->toArray();
		return (int)$result['COUNT(*)'];
	}
	/**
     * Returns new products
     * @param int $count
     * @param int $page
     * @return	array
     */
	public function getNewProducts($count = NULL, $page = NULL)
	{
		$select = $this
			->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
			->setIntegrityCheck(false)
	    	->where('news = ?', 1)
	    	->order('title ASC')
	    	->join('availability',		  
		    	'availability.id = products.availability_id', array('title AS availability'))
			->join('manufacturers',		  
		    	'manufacturers.id = products.manufacturers_id', array('title AS manufacturers', 'title_url AS manufacturers_url'))
			   ;
	    if ($count){
	    	if($page){
	    		$select->limitPage($page, $count);
	    	}else{
	    		$select->limit($count);
	    	}
	    }
		$rows = $this->fetchAll($select);
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$currency = $this->getCurrency();
			$picturesTab = new Eshop_Model_DbTable_Pictures();
			$categoriesTab = new Eshop_Model_DbTable_Categories();
			for ($i=0;$i<count($rows);$i++) {
				$rows[$i]['picture'] = $picturesTab->getPicture($rows[$i]['id']);
				$rows[$i]['price'] = number_format($rows[$i]['price'], 0, '.', ' ').' '.$currency;
				$rows[$i]['price_orig'] = number_format($rows[$i]['price_orig'], 0, '.', ' ').' '.$currency;
				$rows[$i]['category'] = $categoriesTab->getCategoryByProductId($rows[$i]['id']);
			}
			return $rows;
		}
	}
	/**
     * Returns array of product
     * @param	string	$title_url
     * @return	array
     */
	public function getProductByTitleUrl($title_url)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select
			->join('vat',		  
		       		  'vat.id = products.vat_id', array('title AS vat_title', 'vat'))
			->join('availability',		  
		       		  'availability.id = products.availability_id', array('title AS availability_title'))
			->join('manufacturers',		  
		       		  'manufacturers.id = products.manufacturers_id', array('title AS manufacturers', 'title_url AS manufacturers_url'))
			->where('products.title_url = ?', $title_url);
		$row = $this->fetchRow($select);
				
		if ($row){
			$row = $row->toArray();
			$currency = $this->getCurrency();
			$picturesTab = new Eshop_Model_DbTable_Pictures();
			$row['pictures'] = $picturesTab->getPictures($row['id']);
			$row['price_vat'] = number_format(round((100 / ($row['vat'] + 100)) * $row['price']), 0, '.', ' ').' '.$currency;
			$row['price'] = number_format($row['price'], 0, '.', ' ').' '.$currency;
			$row['price_orig'] = number_format($row['price_orig'], 0, '.', ' ').' '.$currency;
			
			$row['vat'] .= ' %';
			return $row;
		}else{
			return NULL;
		}
	}
	/**
     * Return array of products
     * @param	string	$title_url
     * @param	int		$page
     * @return	array
     */
	public function getProductsByCategory($title_url, $page)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('products2categories',		  
		       		  'products2categories.products_id=products.id', array())
			   ->join('categories',		  
		       		  'products2categories.categories_id=categories.id', array())
			   ->join('availability',		  
		       		  'availability.id = products.availability_id', array('title AS availability'))
			   ->join('manufacturers',		  
		       		  'manufacturers.id = products.manufacturers_id', array('title AS manufacturers', 'title_url AS manufacturers_url'))
			   ->where('categories.title_url = ?', $title_url)
		       ->where('products.show = ?', 1)
		       ->where('products.flags IS NULL')
		       ->order('products.title ASC')
		       ->limitPage($page, 8);
		       
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$currency = $this->getCurrency();
			$picturesTab = new Eshop_Model_DbTable_Pictures();
			for ($i=0;$i<count($rows);$i++) {
				$rows[$i]['picture'] = $picturesTab->getPicture($rows[$i]['id']);
				$rows[$i]['price'] = number_format($rows[$i]['price'], 0, '.', ' ').' '.$currency;
				$rows[$i]['price_orig'] = number_format($rows[$i]['price_orig'], 0, '.', ' ').' '.$currency;
			}
			return $rows;
		}
	}
/**
     * Return array of products
     * @param	string	$title_url
     * @param	int		$page
     * @return	array
     */
	public function getProductsByManufactury($title_url, $page)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('availability',		  
		       		  'availability.id = products.availability_id', array('title AS availability'))
			   ->join('manufacturers',		  
		       		  'manufacturers.id = products.manufacturers_id', array('title AS manufacturers', 'title_url AS manufacturers_url'))
			   ->where('manufacturers.title_url = ?', $title_url)
		       ->where('products.show = ?', 1)
		       ->where('products.flags IS NULL')
		       ->order('products.title ASC')
		       ->limitPage($page, 8);
		       
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$currency = $this->getCurrency();
			$picturesTab = new Eshop_Model_DbTable_Pictures();
			for ($i=0;$i<count($rows);$i++) {
				$rows[$i]['picture'] = $picturesTab->getPicture($rows[$i]['id']);
				$rows[$i]['price'] = number_format($rows[$i]['price'], 0, '.', ' ').' '.$currency;
				$rows[$i]['price_orig'] = number_format($rows[$i]['price_orig'], 0, '.', ' ').' '.$currency;
			}
			return $rows;
		}
	}
	/**
     * Return currency
     * @return	string
     */
    private function getCurrency()
    {
    	$settingsTab = new Eshop_Model_DbTable_Settings();
        $currency = $settingsTab->getFlag('eshop');
        return $currency['currency'];
    }
}