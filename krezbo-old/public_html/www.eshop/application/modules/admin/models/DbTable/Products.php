<?php
class Admin_Model_DbTable_Products extends Zend_Db_Table_Abstract
{
	protected $_name = 'products';
	protected $_primary = 'id';
	/**
     * Returns primary keys
     * @param	string	$code
     * @param	string	$title
     * @param	string	$title_menu = ''
     * @param	string	$title_url = ''
     * @param	string	$description = ''
     * @return	int
     */
	public function setProduct($code, $title, $title_menu='', $title_url='', $price, $manufactorers, $vat, $availability, $price_orig, $short_desc, $description='', $show=true, $recommend=false)
	{
		$data = array(
			'code'				=>	($code)?$code:$this->getFreeCode(),
			'title'				=>	$title,
			'title_menu'		=>	($title_menu=='')?$title:$title_menu,
    		'title_url'			=>	($title_url=='')?$this->Convert($title):$this->Convert($title_url),
			'price'				=>	$price,
			'manufacturers_id'	=>	$manufactorers,
			'vat_id'			=>	$vat,
			'availability_id'	=>	$availability,
			'price_orig'		=>	($price_orig)?$price_orig:NULL,
			'short_desc'		=>	$short_desc,
			'description'		=>	$description,
			'show'				=>	$show,
			'recommend'			=>	$recommend
		);
    	return $this->insert($data);    	
	}
	/**
     * Returns array of products
     * @param	int	$order_id
     * @return	array
     */
	public function getProductsByOrderID($order_id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->join('orders2products',		  
		       		  'orders2products.products_id = products.id', array('count'))
			   ->where('orders2products.orders_id = ?', $order_id);
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			
			return $rows;
		}
	}
	/**
     * Returns array of product
     * @param	int	$id
     * @return	array
     */
	public function getProduct($id)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select//->join('pages_bd',		  
		       //		  'pages_bd.pages_id=pages.id', array('show'))
			   ->where('products.id = ?', $id);
		$row = $this->fetchRow($select);
				
		if (count($row)==0) {
			return NULL;
		} else {
			$row = $row->toArray();
			return $row;
		}
	}
	/**
     * Return array of all products
     * @return	array
     */
	public function getProducts($page)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select->order('title ASC')
			->where('flags IS NULL')
			->limitPage($page, 20);
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$products2CategoriesTab = new Admin_Model_DbTable_Products2Categories();
			for ($i=0;$i<count($rows);$i++){
				$rows[$i]['categories_count'] = count($products2CategoriesTab->getProduct($rows[$i]['id']));
			}
			return $rows;
		}
	}
	/**
     * Return array of search products
     * @return	array
     */
	public function searchProducts($text)
	{
		$select = $this->select(Zend_Db_Table::SELECT_WITH_FROM_PART)
					   ->setIntegrityCheck(false);
		$select
			->order('title ASC')
			->where('flags IS NULL')
			->where('LOWER(title) LIKE LOWER(?)', '%'.$text.'%');
		$rows = $this->fetchAll($select);
		
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			$products2CategoriesTab = new Admin_Model_DbTable_Products2Categories();
			for ($i=0;$i<count($rows);$i++){
				$rows[$i]['categories_count'] = count($products2CategoriesTab->getProduct($rows[$i]['id']));
			}
			return $rows;
		}
	}
	/**
     * Update product
     * @param	int	$id
     * @param	string	$code
     * @param	string	$title
     * @param	string	$title_menu = ''
     * @param	string	$title_url = ''
     * @param	string	$description = ''
     * @param	int	$show = 1
     * @return	void
     */
	public function updateProduct($id, $code, $title, $title_menu='', $title_url='', $price, $manufactorers, $vat, $availability, $price_orig='', $short_desc, $description='', $show=true, $recommend=false, $news = NULL)
	{
		$data = array(
			'code'				=>	($code)?$code:$this->getFreeCode(),
			'title'				=>	$title,
			'title_menu'		=>	($title_menu=='')?$title:$title_menu,
    		'title_url'			=>	($title_url=='')?$this->Convert($title):$this->Convert($title_url),
			'price'				=>	$price,
			'manufacturers_id'	=>	$manufactorers,
			'vat_id'			=>	$vat,
			'availability_id'	=>	$availability,
			'price_orig'		=>	($price_orig)?$price_orig:NULL,
			'short_desc'		=>	$short_desc,
			'description'		=>	$description,
			'show'				=>	$show,
			'recommend'			=>	$recommend,
			'news'				=>	$news
		);
		
		$this->update($data, 'id = '.$id);
	}
	
	/**
     * Delete product
     * @param	int	$id
     * @return	void
     */
	public function delProduct($id)
    {
    	$this->setFlag($id, 'delete');
    }
	/**
     * Return converted text
     * @param	string	$text
     * @return	string
     */
	public function Convert($text)
	{
		$ar = array(' '=> '-', '&'=>'-', ':'=>'-', '.'=>'-', ','=>'-', '%'=>'-', '('=>'-', ')'=>'-', '/'=>'-',
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
	/**
     * set flag
	 * @param	int		$product_id
     * @param	string	$tag
     * @return	void
     */
	public function setFlag($product_id, $flag)
    {
    	$data = array('flags'	=>	$flag);
    	$this->update($data, 'id = '.$product_id);
    }
	/**
     * Return currency
     * @return	string
     */
    public function getCurrency()
    {
    	$settingsTab = new Admin_Model_DbTable_Settings();
        $currency = $settingsTab->getFlag('eshop');
        return $currency['currency'];
    }
	/**
     * Returns free code
     * @return	string
     */
	public function getFreeCode()
	{
		$code = sprintf('%09d', rand(0, 999999999));
		$select = $this->select();
		$select->from('products', 'code');
		$rows = $this->fetchAll($select);
				
		if (count($rows)==0) {
			return NULL;
		} else {
			$rows = $rows->toArray();
			foreach ($rows as $row) {
				if ($code == $row['code']){
					$code = $this->getFreeCode();
				}
			}
			return $code;
		}
	}
	/**
     * Return true/false if url of product exist
     * @param	string	$product_url
     * @return	boolean
     */
	public function urlExist($product_url)
	{
		$select = $this->select()->where('title_url = ?', $product_url);
		$rows = $this->fetchAll($select);
		if($rows)
		{
			return count($rows);
		}
		return false;
	}
	/**
     * Return true/false if url of code exist
     * @param	string	$code
     * @return	boolean
     */
	public function codeExist($code)
	{
		$select = $this->select()->where('code = ?', $code);
		$rows = $this->fetchAll($select);
		if($rows)
		{
			return count($rows);
		}
		return false;
	}
	/**
     * Return array of all products for export for zbozi.cz
     * @return	array
     */
	public function getProductsForEsport()
	{
		$select = $this->_db->select()
			->from('products', array('id', 'title', 'title_url', 'short_desc', 'price', 'flags'))
			->order('products.title ASC')
			->where('products.flags IS NULL')
			->group('products.id')
			->join(
				'products2categories',
				'products2categories.products_id = products.id', array())
			->join(
				'categories',
				'categories.id = products2categories.categories_id', array('title AS category_title', 'title_url AS category_url'))
			->join(
				'products2pictures',
				'products2pictures.products_id = products.id', array())
			->join(
				'pictures',
				'pictures.id = products2pictures.pictures_id', array('170x170 AS imgUrl'))
			->where('pictures.active IS TRUE')
			->join(
				'vat',
				'vat.id = products.vat_id', array('vat'))
			->join(
				'manufacturers',
				'manufacturers.id = products.manufacturers_id', array('id AS manufacturer_id', 'title AS manufacturer'))
			;
		$result = $this->_db->fetchAll($select);
		
		$front = Zend_Controller_Front::getInstance();
		$server = $front->getRequest()->getHttpHost();
		$viewRenderer = Zend_Controller_Action_HelperBroker::getStaticHelper('viewRenderer');
		$view = $viewRenderer->view;
		
		if (count($result)==0) {
			return NULL;
		} else {
			for ($i=0;$i<count($result);$i++){
				$result[$i]['url'] = 'http://'.$server.$view->url(array(
					'category'	=>	$result[$i]['category_url'],
					'product'	=>	$result[$i]['title_url']),
					'eshop_index_product');
				$result[$i]['imgUrl'] = 'http://'.$server.'/'.$result[$i]['imgUrl'];
				$result[$i]['priceVat'] = $result[$i]['price'];
				$result[$i]['price'] = round((100 / ($result[$i]['vat'] + 100)) * $result[$i]['price']);
				if ($result[$i]['manufacturer_id'] == 1) {
					$result[$i]['manufacturer'] = NULL;
				}
			}
			return $result;
		}
	}
}